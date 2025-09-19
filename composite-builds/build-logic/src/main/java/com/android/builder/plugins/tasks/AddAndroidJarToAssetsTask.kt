package com.android.builder.plugins.tasks

import com.android.builder.build.config.BuildConfig
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Adds the `android.jar` file from the Android SDK to assets.
 */
abstract class AddAndroidJarToAssetsTask : DefaultTask() {

    /**
     * Path to the `android.jar` file.
     */
    @get:Internal
    internal var androidJar: File? = null

    /**
     * The output directory to copy the `android.jar` file to.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun addAndroidJarToAssets() {
        val outFile = outputDirectory.dir("data/common").get().asFile.also { dir -> dir.mkdirs() }
            .let { dir -> File(dir, "android.jar") }

        val androidJar = this.androidJar
            ?: throw GradleException("androidJar is not set for AddAndroidJarToAssetsTask")

        if (!androidJar.exists() || !androidJar.isFile) {
            throw GradleException("File $androidJar does not exist or is not a file.")
        }

        androidJar.copyTo(outFile, overwrite = true)
    }
}
