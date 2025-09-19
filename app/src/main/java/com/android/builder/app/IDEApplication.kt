/*
 * This file is part of Android-builder.
 *
 * Android-builder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android-builder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android-builder.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.android.builder.app

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.blankj.utilcode.util.ThrowableUtils.getFullStackTrace
import com.google.android.material.color.DynamicColors
import com.android.builder.BuildConfig
import com.android.builder.activities.CrashHandlerActivity
import com.android.builder.activities.editor.IDELogcatReader
import com.android.builder.buildinfo.BuildInfo
import com.android.builder.editor.schemes.IDEColorSchemeProvider
import com.android.builder.eventbus.events.preferences.PreferenceChangeEvent
import com.android.builder.events.AppEventsIndex
import com.android.builder.events.EditorEventsIndex
import com.android.builder.events.LspApiEventsIndex
import com.android.builder.events.LspJavaEventsIndex
import com.android.builder.preferences.internal.DevOpsPreferences
import com.android.builder.preferences.internal.GeneralPreferences
import com.android.builder.preferences.internal.StatPreferences
import com.android.builder.resources.localization.LocaleProvider
import com.android.builder.stats.AndroidBuilderStats
import com.android.builder.stats.StatUploadWorker
import com.android.builder.syntax.colorschemes.SchemeAndroidBuilder
import com.android.builder.treesitter.TreeSitter
import com.android.builder.ui.themes.IDETheme
import com.android.builder.ui.themes.IThemeManager
import com.android.builder.utils.RecyclableObjectPool
import com.android.builder.utils.VMUtils
import com.android.builder.utils.flashError
import com.termux.app.TermuxApplication
import com.termux.shared.reflection.ReflectionUtils
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import java.lang.Thread.UncaughtExceptionHandler
import java.time.Duration
import kotlin.system.exitProcess

class IDEApplication : TermuxApplication() {

    private var uncaughtExceptionHandler: UncaughtExceptionHandler? = null
    private var ideLogcatReader: IDELogcatReader? = null

    init {
        if (!VMUtils.isJvm()) {
            TreeSitter.loadLibrary()
        }
        RecyclableObjectPool.DEBUG = BuildConfig.DEBUG
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        instance = this
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, th -> handleCrash(thread, th) }

        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                    .penaltyLog()
                    .detectAll()
                    .build()
            )
            if (DevOpsPreferences.dumpLogs) {
                startLogcatReader()
            }
        }

        EventBus.builder()
            .addIndex(AppEventsIndex())
            .addIndex(EditorEventsIndex())
            .addIndex(LspApiEventsIndex())
            .addIndex(LspJavaEventsIndex())
            .installDefaultEventBus(true)

        EventBus.getDefault().register(this)

        AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)

        if (IThemeManager.getInstance().getCurrentTheme() == IDETheme.MATERIAL_YOU) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        EditorColorScheme.setDefault(SchemeAndroidBuilder.newInstance(null))

        ReflectionUtils.bypassHiddenAPIReflectionRestrictions()
        GlobalScope.launch {
            IDEColorSchemeProvider.init()
        }
    }

    fun showChangelog() {
        val intent = Intent(Intent.ACTION_VIEW)
        var version = BuildInfo.VERSION_NAME_SIMPLE
        if (!version.startsWith('v')) {
            version = "v$version"
        }
        intent.data = Uri.parse("${BuildInfo.REPO_URL}/releases/tag/$version")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (th: Throwable) {
            log.error("Unable to start activity to show changelog", th)
            flashError("Unable to start activity")
        }
    }

    fun reportStatsIfNecessary() {
        if (!StatPreferences.statOptIn) {
            log.info("Stat collection is disabled.")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<StatUploadWorker>(Duration.ofHours(24))
            .setInputData(AndroidBuilderStats.statData.toInputData())
            .setConstraints(constraints)
            .addTag(StatUploadWorker.WORKER_WORK_NAME)
            .build()

        val workManager = WorkManager.getInstance(this)

        log.info("reportStatsIfNecessary: Enqueuing StatUploadWorker...")
        val operation = workManager.enqueueUniquePeriodicWork(
            StatUploadWorker.WORKER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        operation.state.observeForever(object : Observer<Operation.State> {
            override fun onChanged(value: Operation.State) {
                operation.state.removeObserver(this)
                log.debug("reportStatsIfNecessary: WorkManager enqueue result: {}", value)
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPrefChanged(event: PreferenceChangeEvent) {
        val enabled = event.value as? Boolean?
        when (event.key) {
            StatPreferences.STAT_OPT_IN -> {
                if (enabled == true) {
                    reportStatsIfNecessary()
                } else {
                    cancelStatUploadWorker()
                }
            }
            DevOpsPreferences.KEY_DEVOPTS_DEBUGGING_DUMPLOGS -> {
                if (enabled == true) startLogcatReader() else stopLogcatReader()
            }
            GeneralPreferences.UI_MODE -> {
                if (GeneralPreferences.uiMode != AppCompatDelegate.getDefaultNightMode()) {
                    AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)
                }
            }
            GeneralPreferences.SELECTED_LOCALE -> {
                val selectedLocale = GeneralPreferences.selectedLocale
                val localeListCompat = selectedLocale?.let {
                    LocaleListCompat.create(LocaleProvider.getLocale(selectedLocale))
                } ?: LocaleListCompat.getEmptyLocaleList()

                AppCompatDelegate.setApplicationLocales(localeListCompat)
            }
        }
    }

    private fun handleCrash(thread: Thread, th: Throwable) {
        writeException(th)
        try {
            val intent = Intent()
            intent.action = CrashHandlerActivity.REPORT_ACTION
            intent.putExtra(CrashHandlerActivity.TRACE_KEY, getFullStackTrace(th))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            uncaughtExceptionHandler?.uncaughtException(thread, th)
            exitProcess(1)
        } catch (error: Throwable) {
            log.error("Unable to show crash handler activity", error)
        }
    }

    private fun cancelStatUploadWorker() {
        log.info("Opted-out of stat collection. Cancelling StatUploadWorker if enqueued...")
        val operation = WorkManager.getInstance(this)
            .cancelUniqueWork(StatUploadWorker.WORKER_WORK_NAME)
        operation.state.observeForever(object : Observer<Operation.State> {
            override fun onChanged(value: Operation.State) {
                operation.state.removeObserver(this)
                log.info("StatUploadWorker: Cancellation result state: {}", value)
            }
        })
    }

    private fun startLogcatReader() {
        if (ideLogcatReader != null) return
        log.info("Starting logcat reader...")
        ideLogcatReader = IDELogcatReader().also { it.start() }
    }

    private fun stopLogcatReader() {
        log.info("Stopping logcat reader...")
        ideLogcatReader?.stop()
        ideLogcatReader = null
    }

    companion object {
        private val log = LoggerFactory.getLogger(IDEApplication::class.java)

        @JvmStatic
        lateinit var instance: IDEApplication
            private set
    }
}
