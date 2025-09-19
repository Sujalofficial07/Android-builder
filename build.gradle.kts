@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("build-logic.root-project")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.gradle.publish) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.benchmark) apply false
}

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.nav.safe.args.gradle.plugin)
    }
}

// Root project group ID set to new package
project.group = "com.android.builder"

subprojects {
    if (project != rootProject) {
        var group = project.parent!!.group
        if (project.parent != rootProject) {
            group = "${group}.${project.parent!!.name}"
        }
        project.group = group
    }

    project.version = rootProject.version

    plugins.withId("com.android.application") {
        // Configure Android modules
        configure<com.android.build.gradle.internal.dsl.BaseAppModuleExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    plugins.withId("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    plugins.withId("java-library") {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xstring-concat=inline"
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
