package com.android.builder.plugins.tasks

import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import java.io.File

/**
 * Generates Gradle wrapper files for Android-builder.
 *
 * Replaces the old IDEWrapperGenerator.
 */
class BuilderWrapperGenerator {

    /**
     * The Gradle version to generate the wrapper for.
     */
    var gradleVersion: String = "7.4.2"

    /**
     * The distribution type (default = BIN).
     */
    var distributionType: Wrapper.DistributionType = Wrapper.DistributionType.BIN

    /**
     * Path to the generated `gradle-wrapper.jar`.
     */
    lateinit var jarFile: File

    /**
     * Path to the generated `gradlew` script.
     */
    lateinit var scriptFile: File

    /**
     * Generate the Gradle wrapper files.
     */
    fun generate(project: Project) {
        val wrapper = project.tasks.create("customWrapper", Wrapper::class.java).apply {
            gradleVersion = this@BuilderWrapperGenerator.gradleVersion
            distributionType = this@BuilderWrapperGenerator.distributionType
            jarFile = this@BuilderWrapperGenerator.jarFile
            scriptFile = this@BuilderWrapperGenerator.scriptFile
        }

        // Execute the wrapper task to generate files
        wrapper.actions.forEach { action ->
            action.execute(wrapper)
        }

        // Cleanup the temporary wrapper task
        project.tasks.remove(wrapper)
    }
}
