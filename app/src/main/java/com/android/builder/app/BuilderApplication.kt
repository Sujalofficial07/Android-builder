package com.sujal.builder.app

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blankj.utilcode.util.ThrowableUtils.getFullStackTrace
import com.google.android.material.color.DynamicColors
import com.sujal.builder.BuildConfig
import com.sujal.builder.activities.CrashHandlerActivity
import com.sujal.builder.activities.editor.BuilderLogcatReader
import com.sujal.builder.app.configuration.BuilderBuildConfigProvider
import com.sujal.builder.preferences.internal.DevOpsPreferences
import com.sujal.builder.preferences.internal.GeneralPreferences
import com.sujal.builder.preferences.internal.StatPreferences
import com.sujal.builder.resources.localization.LocaleProvider
import com.sujal.builder.stats.BuilderStats
import com.sujal.builder.stats.StatUploadWorker
import com.sujal.builder.syntax.colorschemes.SchemeBuilder
import com.sujal.builder.ui.themes.BuilderTheme
import com.sujal.builder.ui.themes.IThemeManager
import com.sujal.builder.utils.RecyclableObjectPool
import com.sujal.builder.utils.VMUtils
import com.sujal.builder.utils.flashError
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

class BuilderApplication : TermuxApplication() {

    private var uncaughtExceptionHandler: UncaughtExceptionHandler? = null
    private var logcatReader: BuilderLogcatReader? = null

    init {
        if (!VMUtils.isJvm()) {
            // Load native libs if needed
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
                StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy()).penaltyLog().detectAll().build()
            )
            if (DevOpsPreferences.dumpLogs) {
                startLogcatReader()
            }
        }

        // Simplified EventBus (no index)
        EventBus.builder().installDefaultEventBus(true)

        EventBus.getDefault().register(this)

        AppCompatDelegate.setDefaultNightMode(GeneralPreferences.uiMode)

        if (IThemeManager.getInstance().getCurrentTheme() == BuilderTheme.MATERIAL_YOU) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        EditorColorScheme.setDefault(SchemeBuilder.newInstance(null))

        ReflectionUtils.bypassHiddenAPIReflectionRestrictions()
        GlobalScope.launch {
            // Init color scheme provider, etc.
        }
    }

    fun showChangelog() {
        val intent = Intent(Intent.ACTION_VIEW)
        var version = BuildConfig.VERSION_NAME
        if (!version.startsWith('v')) {
            version = "v$version"
        }
        intent.data = Uri.parse("https://github.com/yourrepo/releases/tag/$version")
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

        log.info("Enqueuing StatUploadWorker...")
        val operation = workManager.enqueueUniquePeriodicWork(
            StatUploadWorker.WORKER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, request
        )

        operation.state.observeForever(object : Observer<Operation.State> {
            override fun onChanged(value: Operation.State) {
                operation.state.removeObserver(this)
                log.debug("WorkManager enqueue result: {}", value)
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPrefChanged(event: Any) {
        // handle pref changes if needed
    }

    private fun handleCrash(thread: Thread, th: Throwable) {
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

    private fun startLogcatReader() {
        if (logcatReader != null) return
        log.info("Starting logcat reader...")
        logcatReader = BuilderLogcatReader().also { it.start() }
    }

    private fun stopLogcatReader() {
        log.info("Stopping logcat reader...")
        logcatReader?.stop()
        logcatReader = null
    }

    companion object {
        private val log = LoggerFactory.getLogger(BuilderApplication::class.java)

        @JvmStatic
        lateinit var instance: BuilderApplication
            private set
    }
}
