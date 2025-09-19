package com.android.builder.preferences.internal

import androidx.appcompat.app.AppCompatDelegate

/**
 * General app preferences for Android-builder.
 */
object GeneralPreferences {

    /** Current UI mode (day/night/auto). */
    var uiMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    /** Selected locale string (null = system default). */
    var selectedLocale: String? = null

    /** Preference key constants. */
    const val UI_MODE = "pref_ui_mode"
    const val SELECTED_LOCALE = "pref_selected_locale"
}
