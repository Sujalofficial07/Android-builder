package com.android.builder.plugins.conf

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.maven

/**
 * Configures Maven publishing for modules.
 *
 * Rebranded for Android-builder.
 */
fun Project.configureMavenPublish(repoUrl: String, groupId: String, artifactId: String, version: String) {
    extensions.configure<PublishingExtension> {
        publications {
            // Add more publication types if needed
        }
        repositories {
            maven {
                url = uri(repoUrl)
            }
        }
    }

    this.group = groupId
    this.version = version
}
