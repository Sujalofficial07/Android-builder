package com.android.builder.plugins.tasks

import com.android.builder.build.config.VersionUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Generates the Gradle init script for Android-builder.
 */
abstract class GenerateInitScriptTask : DefaultTask() {

    @get:Input
    abstract val downloadVersion: Property<String>

    @get:Input
    abstract val mavenGroupId: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outFile = this.outputDir.file("data/common/androidbuilder.init.gradle")
            .also {
                it.get().asFile.parentFile.mkdirs()
            }

        outFile.get().asFile.bufferedWriter().use { writer ->
            writer.write(
                """
                initscript {
                    repositories {
                        // Always specify the snapshots repository first
                        maven {
                            // Add snapshots repository for Android-builder CI builds
                            url "${VersionUtils.SONATYPE_SNAPSHOTS_REPO}"
                        }
                        
                        maven {
                            // Add public repository for Android-builder release builds
                            url "${VersionUtils.SONATYPE_PUBLIC_REPO}"
                        }
                        
                        mavenCentral()
                        google()
                    }

                    dependencies {
                        classpath('${mavenGroupId.get()}:gradle-plugin:${downloadVersion.get()}') {
                            setChanging(false)
                        }
                    }
                }
                
                apply plugin: com.android.builder.gradle.AndroidBuilderInitScriptPlugin
                """.trimIndent()
            )
        }
    }
}
