@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("composite-builds/build-logic") {
        name = "build-logic"
    }

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/public/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Android-builder"

// keep this sorted alphabetically
include(
    ":annotation:annotations",
    ":annotation:processors",
    ":annotation:processors-ksp",
    ":core:actions",
    ":core:app",
    ":core:common",
    ":core:indexing-api",
    ":core:indexing-core",
    ":core:lsp-api",
    ":core:lsp-models",
    ":core:projects",
    ":core:resources",
    ":editor:api",
    ":editor:impl",
    ":editor:lexers",
    ":editor:treesitter",
    ":event:eventbus",
    ":event:eventbus-android",
    ":event:eventbus-events",
    ":java:javac-services",
    ":java:lsp",
    ":logging:idestats",
    ":logging:logger",
    ":logging:logsender",
    ":termux:application",
    ":termux:emulator",
    ":termux:shared",
    ":termux:view",
    ":testing:androidTest",
  //  ":testing:benchmarks",
    ":testing:commonTest",
    ":testing:gradleToolingTest",
    ":testing:lspTest",
    ":testing:unitTest",
    ":tooling:api",
    ":tooling:builder-model-impl",
    ":tooling:events",
    ":tooling:impl",
    ":tooling:model",
    ":tooling:plugin",
    ":tooling:plugin-config",
    ":utilities:build-info",
    ":utilities:flashbar",
    ":utilities:framework-stubs",
    ":utilities:lookup",
    ":utilities:preferences",
    ":utilities:shared",
    ":utilities:templates-api",
    ":utilities:templates-impl",
    ":utilities:treeview",
    ":utilities:uidesigner",
    ":utilities:xml-inflater",
    ":xml:aaptcompiler",
    ":xml:dom",
    ":xml:lsp",
    ":xml:resources-api",
    ":xml:utils",
)
