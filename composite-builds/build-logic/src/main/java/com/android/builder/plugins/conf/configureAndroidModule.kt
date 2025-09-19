package com.android.builder.plugins.conf

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Configures Android modules with common settings.
 *
 * Rebranded for Android-builder.
 */
fun Project.configureAndroidModule(desugaringDep: Any) {
    extensions.configure(BaseExtension::class.java) { ext ->
        ext.compileSdkVersion(34)

        ext.defaultConfig {
            minSdk = 26
            targetSdk = 28
        }

        // Enable Java 11 source/target
        ext.compileOptions.apply {
            sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11
            targetCompatibility = org.gradle.api.JavaVersion.VERSION_11
        }

        dependencies {
            add("coreLibraryDesugaring", desugaringDep)
        }
    }
}
