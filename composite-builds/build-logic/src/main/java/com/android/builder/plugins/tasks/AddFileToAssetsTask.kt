package com.android.builder.plugins.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Adds the provided file to assets.
 */
abstract class AddFileToAssetsTask : DefaultTask() {

    /**
     * The input file that should be copied to the assets directory.
     */
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    /**
     * The base assets path. The file will be saved in assets to `base-path/file-name`.
     */
    @get:Input
    abstract val baseAssetsPath: Property<String>

    /**
     * The file name of the file in assets. The file will be saved in assets to `base-path/file-name`.
     */
    @get:Input
    @get:Optional
    abstract val fileName: Property<String>

    /**
     * The output assets directory. This should not be set manually, but provided to the Android Gradle Plugin.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun copy() {
        var basePath = baseAssetsPath.get()
        if (basePath.isBlank()) {
            basePath = "data"
        }

        while (basePath.endsWith('/')) {
            basePath = basePath.removeSuffix("/")
        }

        val inFile = inputFile.get().asFile

        require(inFile.exists()) { "File '$inFile' does not exist" }
        require(inFile.isFile) { "File '$inFile' is not a file" }

        var outFileName = fileName.orNull ?: inFile.name
        if (outFileName.isBlank()) {
            outFileName = inFile.name
        }

        val outFile = outputDirectory.file("$basePath/$outFileName").get().asFile.also {
            it.parentFile.mkdirs()
        }

        inFile.inputStream().buffered().use { input ->
            outFile.outputStream().buffered().use { output ->
                input.transferTo(output)
            }
        }
    }
}
