package com.android.builder.plugins

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.builder.build.config.BuildConfig
import com.android.builder.build.config.downloadVersion
import com.android.builder.plugins.tasks.AddAndroidJarToAssetsTask
import com.android.builder.plugins.tasks.AddFileToAssetsTask
import com.android.builder.plugins.tasks.GenerateInitScriptTask
import com.android.builder.plugins.tasks.GradleWrapperGeneratorTask
import com.android.builder.plugins.tasks.SetupAapt2Task
import com.android.builder.plugins.util.SdkUtils.getAndroidJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

/**
 * Handles asset copying and generation for Android-builder.
 */
class AndroidBuilderAssetsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            val wrapperGeneratorTaskProvider =
                tasks.register("generateGradleWrapper", GradleWrapperGeneratorTask::class.java)

            val androidComponentsExtension =
                extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            val setupAapt2TaskTaskProvider =
                tasks.register("setupAapt2", SetupAapt2Task::class.java)

            val addAndroidJarTaskProvider =
                tasks.register("addAndroidJarToAssets", AddAndroidJarToAssetsTask::class.java) {
                    androidJar = androidComponentsExtension.getAndroidJar(assertExists = true)
                }

            androidComponentsExtension.onVariants { variant ->

                val variantNameCapitalized = variant.name.capitalized()

                variant.sources.jniLibs?.addGeneratedSourceDirectory(
                    setupAapt2TaskTaskProvider,
                    SetupAapt2Task::outputDirectory
                )

                variant.sources.assets?.addGeneratedSourceDirectory(
                    wrapperGeneratorTaskProvider,
                    GradleWrapperGeneratorTask::outputDirectory
                )

                variant.sources.assets?.addGeneratedSourceDirectory(
                    addAndroidJarTaskProvider,
                    AddAndroidJarToAssetsTask::outputDirectory
                )

                // Init script generator
                val generateInitScript =
                    tasks.register("generate${variantNameCapitalized}InitScript", GenerateInitScriptTask::class.java) {
                        mavenGroupId.set(BuildConfig.packageName)
                        downloadVersion.set(this@run.downloadVersion)
                    }

                variant.sources.assets?.addGeneratedSourceDirectory(
                    generateInitScript,
                    GenerateInitScriptTask::outputDir
                )

                // Tooling API JAR copier
                val copyToolingApiJar =
                    tasks.register("copy${variantNameCapitalized}ToolingApiJar", AddFileToAssetsTask::class.java) {
                        val implPath = ":tooling:impl"
                        val toolingApi = checkNotNull(rootProject.findProject(implPath)) {
                            "Cannot find the Tooling Impl module with project path: '$implPath'"
                        }
                        dependsOn(toolingApi.tasks.getByName("copyJar"))

                        val toolingApiJar = toolingApi.layout.buildDirectory.file("libs/tooling-api-all.jar")

                        inputFile.set(toolingApiJar)
                        baseAssetsPath.set("data/common")
                    }

                variant.sources.assets?.addGeneratedSourceDirectory(
                    copyToolingApiJar,
                    AddFileToAssetsTask::outputDirectory
                )
            }
        }
    }
}
