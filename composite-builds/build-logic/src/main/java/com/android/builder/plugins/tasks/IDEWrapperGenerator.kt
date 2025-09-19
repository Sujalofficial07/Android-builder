package com.android.builder.plugins.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import java.io.File

/**
 * Generates Gradle Wrapper scripts and jar.
 *
 * Rebranded for Android-builder.
 */
class IDEWrapperGenerator {

    /** Path to the gradle-wrapper.jar file */
    lateinit var jarFile: File

    /** Path to the gradlew script file */
    lateinit var scriptFile: File

    /** Gradle version to generate */
    var gradleVersion: String = "7.4.2"

    /** Distribution type (BIN/ALL) */
    var distributionType: Wrapper.DistributionType = Wrapper.DistributionType.BIN

    /**
     * Generate wrapper files for the given project.
     */
    fun generate(project: Project) {
        val wrapperTask = project.tasks.create("tempWrapper", Wrapper::class.java) {
            gradleVersion = this@IDEWrapperGenerator.gradleVersion
            distributionType = this@IDEWrapperGenerator.distributionType
            jarFile = this@IDEWrapperGenerator.jarFile
            scriptFile = this@IDEWrapperGenerator.scriptFile
        }

        wrapperTask.generateWrapper()
        project.tasks.remove(wrapperTask)
    }
}
