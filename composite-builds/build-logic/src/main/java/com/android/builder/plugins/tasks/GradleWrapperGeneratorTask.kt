package com.android.builder.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.wrapper.Wrapper
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Generates the `gradle-wrapper.zip` file for Android-builder.
 */
abstract class GradleWrapperGeneratorTask : DefaultTask() {

    /**
     * The output directory.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    companion object {
        private const val GRADLE_VERSION = "7.4.2"
    }

    @TaskAction
    fun generateGradleWrapperZip() {
        val outputDirectory = this.outputDirectory.get().file("data/common").asFile
        outputDirectory.mkdirs()

        val destFile = outputDirectory.resolve("gradle-wrapper.zip")

        if (destFile.exists()) {
            destFile.delete()
        }

        val stagingDir = File(outputDirectory, "staging")
        if (stagingDir.exists()) {
            stagingDir.deleteRecursively()
        }
        stagingDir.mkdirs()

        // Generate the files
        val generator = BuilderWrapperGenerator() // renamed from IDEWrapperGenerator
        generator.jarFile = File(stagingDir, "gradle/wrapper/gradle-wrapper.jar")
        generator.scriptFile = File(stagingDir, "gradlew")
        generator.gradleVersion = GRADLE_VERSION
        generator.distributionType = Wrapper.DistributionType.BIN

        generator.generate(project)

        // Archive all generated files
        ZipOutputStream(destFile.outputStream().buffered()).use { zipOut ->
            stagingDir.walk(direction = FileWalkDirection.TOP_DOWN)
                .filter { it.isFile }
                .forEach { file ->
                    val entry = ZipEntry(file.relativeTo(stagingDir).path)
                    zipOut.putNextEntry(entry)
                    file.inputStream().buffered().use { fileInStream ->
                        fileInStream.transferTo(zipOut)
                    }
                }

            zipOut.flush()
        }

        // finally, delete the staging directory
        stagingDir.deleteRecursively()
    }
}
