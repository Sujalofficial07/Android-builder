package com.android.builder.plugins.util

import org.gradle.api.Project

/**
 * Extension utilities for Gradle plugins.
 *
 * Rebranded for Android-builder.
 */
val Project.isAndroidModule: Boolean
    get() = plugins.hasPlugin("com.android.library") || plugins.hasPlugin("com.android.application")

val Project.isApplicationModule: Boolean
    get() = plugins.hasPlugin("com.android.application")

val Project.isLibraryModule: Boolean
    get() = plugins.hasPlugin("com.android.library")
    
