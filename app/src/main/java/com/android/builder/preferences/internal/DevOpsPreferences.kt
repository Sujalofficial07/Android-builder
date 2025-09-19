package com.android.builder.preferences.internal

/**
 * Developer & Ops preferences for Android-builder.
 */
object DevOpsPreferences {

    /** Whether debug logcat dumping is enabled. */
    var dumpLogs: Boolean = false

    /** Preference key constant. */
    const val KEY_DEVOPTS_DEBUGGING_DUMPLOGS = "pref_devops_debugging_dumplogs"
}
