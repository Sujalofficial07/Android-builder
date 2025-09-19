package com.android.builder.plugins

import com.android.build.gradle.BaseExtension
import com.android.builder.build.config.isFDroidBuild
import com.android.builder.plugins.util.isAndroidModule
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Manages plugins applied on the Android-builder project modules.
 */
class AndroidBuilderPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        if (project.path == rootProject.path) {
            throw GradleException("Cannot apply ${AndroidBuilderPlugin::class.simpleName} to root project")
        }

        if (!project.buildFile.exists() || !project.buildFile.isFile) {
            return@run
        }

        if (isAndroidModule && !isFDroidBuild) {
            // setup signing configuration
            plugins.apply(SigningConfigPlugin::class.java)
        }

        if (isFDroidBuild && project.plugins.hasPlugin("com.android.builder.core-app")) {
            val baseExtension = extensions.getByType(BaseExtension::class.java)
            logger.warn("Building for F-Droid with configuration:")
            logger.warn("applicationId = ${baseExtension.defaultConfig.applicationId}")
            logger.warn("versionName = ${baseExtension.defaultConfig.versionName}")
            logger.warn("versionCode = ${baseExtension.defaultConfig.versionCode}")
            logger.warn("--- x --- x ---")
        }

        val taskName = when {
            isAndroidModule -> "testDebugUnitTest"
            else -> "test"
        }

        logger.info("${project.path} will run task '$taskName' for tests in CI")

        project.tasks.create("runTestsInCI") {
            dependsOn(taskName)
        }
    }
}
