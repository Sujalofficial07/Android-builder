package com.android.builder.plugins.util

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import java.io.File

/**
 * Utility functions for accessing Android SDK artifacts.
 *
 * Rebranded for Android-builder.
 */
object SdkUtils {

    /**
     * Locate the `android.jar` for the given project.
     *
     * @param project Gradle project
     * @param assertExists whether to throw if the JAR does not exist
     * @return File pointing to `android.jar`, or null if not found
     */
    @JvmStatic
    fun Project.getAndroidJar(assertExists: Boolean = false): File? {
        val extension = extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
            ?: return null

        val sdkDir = File(
            System.getenv("ANDROID_HOME")
                ?: System.getenv("ANDROID_SDK_ROOT")
                ?: return null
        )

        val compileSdkVersion = extension.sdkComponents.compileSdkVersion.get()
        val androidJar = File(sdkDir, "platforms/$compileSdkVersion/android.jar")

        if (assertExists) {
            require(androidJar.exists() && androidJar.isFile) {
                "android.jar not found for SDK $compileSdkVersion at $androidJar"
            }
        }

        return androidJar.takeIf { it.exists() }
    }
}
