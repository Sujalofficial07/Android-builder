package com.android.builder.build.config

import org.gradle.api.JavaVersion

/**
 * Build configuration for Android-builder.
 */
object BuildConfig {

    /** Android-builder's package name. */
    const val packageName = "com.android.builder"

    /** The compile SDK version. */
    const val compileSdk = 34

    /** The minimum SDK version. */
    const val minSdk = 26

    /** The target SDK version. */
    const val targetSdk = 28

    const val ndkVersion = "26.1.10909125"

    /** The source and target Java compatibility. */
    val javaVersion = JavaVersion.VERSION_11
}
