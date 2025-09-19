package com.android.builder.plugins.conf

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

/**
 * Configures Java-only modules.
 *
 * Rebranded for Android-builder.
 */
fun Project.configureJavaModule() {
    extensions.configure(JavaPluginExtension::class.java) { ext ->
        ext.sourceCompatibility = JavaVersion.VERSION_11
        ext.targetCompatibility = JavaVersion.VERSION_11
    }
}
