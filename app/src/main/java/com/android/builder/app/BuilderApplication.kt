package com.android.builder.app

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.android.builder.BuildConfig
import com.android.builder.activities.CrashHandlerActivity
import com.android.builder.activities.editor.BuilderLogcatReader
import com.android.builder.editor.schemes.BuilderColorSchemeProvider
import com.android.builder.eventbus.events.preferences.PreferenceChangeEvent
import com.android.builder.preferences.internal.DevOpsPreferences
import com.android.builder.preferences.internal.GeneralPreferences
import com.android.builder.preferences.internal.StatPreferences
import com.android.builder.resources.localization.LocaleProvider
import com.android.builder.stats.BuilderStats
import com.android.builder.stats.StatUploadWorker
import com.android.builder.syntax.colorschemes.SchemeBuilder
import com.android.builder.ui.themes.BuilderTheme
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

/**
 * Main application class for Android-builder.
 */
class BuilderApplication : TermuxApplication() {

    private var uncaughtExceptionHandler: UncaughtExceptionHandler? = null
    private var logcatReader: BuilderLogcatReader? = null

    init {
        if (!VMUtils.isJvm()) {
            // load native parsers
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

        EventBus.builder().installDefaultEventBus(true)
        EventBus.getDefault().register(this)

        AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)

        if (IThemeManager.getInstance().getCurrentTheme() == BuilderTheme.MATERIAL_YOU) {
            // apply material you if supported
        }

        EditorColorScheme.setDefault(SchemeBuilder.newInstance(null))

        ReflectionUtils.bypassHiddenAPIReflectionRestrictions()
        GlobalScope.launch { BuilderColorSchemeProvider.init() }
    }

    fun showChangelog() {
        val intent = Intent(Intent.ACTION_VIEW)
        val version = "v${BuildConfig.VERSION_NAME}"
        intent.data = Uri.parse("https://github.com/yourrepo/android-builder/releases/tag/$version")
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
            .setInputData(BuilderStats.statData.toInputData())
            .setConstraints(constraints)
            .addTag(StatUploadWorker.WORKER_WORK_NAME)
            .build()

        val workManager = WorkManager.getInstance(this)
        val operation = workManager.enqueueUniquePeriodicWork(
            StatUploadWorker.WORKER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        operation.state.observeForever(object : Observer<Operation.State> {
            override fun onChanged(value: Operation.State) {
                operation.state.removeObserver(this)
                log.debug("Stat worker enqueue result: {}", value)
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPrefChanged(event: PreferenceChangeEvent) {
        val enabled = event.value as? Boolean?
        if (event.key == StatPreferences.STAT_OPT_IN) {
            if (enabled == true) reportStatsIfNecessary() else cancelStatUploadWorker()
        } else if (event.key == DevOpsPreferences.KEY_DEVOPTS_DEBUGGING_DUMPLOGS) {
            if (enabled == true) startLogcatReader() else stopLogcatReader()
        } else if (event.key == GeneralPreferences.UI_MODE &&
            GeneralPreferences.uiMode != AppCompatDelegate.getDefaultNightMode()
        ) {
            AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)
        } else if (event.key == GeneralPreferences.SELECTED_LOCALE) {
            val selectedLocale = GeneralPreferences.selectedLocale
            val localeListCompat = selectedLocale?.let {
                LocaleListCompat.create(LocaleProvider.getLocale(selectedLocale))
            } ?: LocaleListCompat.getEmptyLocaleList()
            AppCompatDelegate.setApplicationLocales(localeListCompat)
        }
    }

    private fun handleCrash(thread: Thread, th: Throwable) {
        try {
            val intent = Intent().apply {
                action = CrashHandlerActivity.REPORT_ACTION
                putExtra(CrashHandlerActivity.TRACE_KEY, th.stackTraceToString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            uncaughtExceptionHandler?.uncaughtException(thread, th)
            exitProcess(1)
        } catch (error: Throwable) {
            log.error("Unable to show crash handler", error)
        }
    }

    private fun cancelStatUploadWorker() {
        val operation = WorkManager.getInstance(this)
            .cancelUniqueWork(StatUploadWorker.WORKER_WORK_NAME)
        operation.state.observeForever(object : Observer<Operation.State> {
            override fun onChanged(value: Operation.State) {
                operation.state.removeObserver(this)
                log.info("Stat worker cancelled: {}", value)
            }
        })
    }

    private fun startLogcatReader() {
        if (logcatReader != null) return
        log.info("Starting logcat reader…")
        logcatReader = BuilderLogcatReader().also { it.start() }
    }

    private fun stopLogcatReader() {
        log.info("Stopping logcat reader…")
        logcatReader?.stopReader()
        logcatReader = null
    }

    companion object {
        private val log = LoggerFactory.getLogger(BuilderApplication::class.java)

        @JvmStatic
        lateinit var instance: BuilderApplication
            private set
    }
}
