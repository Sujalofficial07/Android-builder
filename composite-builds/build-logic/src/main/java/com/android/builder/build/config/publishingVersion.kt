package com.android.builder.build.config

import org.gradle.api.Project

/**
 * Extension for determining publishing version.
 *
 * Rebranded for Android-builder.
 */
val Project.publishingVersion: String
    get() {
        val versionProp = findProperty("VERSION_NAME")?.toString()
        return versionProp ?: "0.0.1-SNAPSHOT"
    }
    
