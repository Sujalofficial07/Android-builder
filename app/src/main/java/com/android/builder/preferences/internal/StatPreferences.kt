package com.android.builder.preferences.internal

/**
 * Stats collection preferences for Android-builder.
 */
object StatPreferences {

    /** Whether user opted-in to usage statistics. */
    var statOptIn: Boolean = false

    /** Preference key constant. */
    const val STAT_OPT_IN = "pref_stat_opt_in"
}
