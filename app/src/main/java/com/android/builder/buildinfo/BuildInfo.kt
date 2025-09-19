package com.android.builder.buildinfo

import com.android.builder.BuildConfig

/**
 * Project metadata and CI build info for Android-builder.
 * Replace values with your own if using CI/CD or custom release channels.
 */
object BuildInfo {
    // App version (from BuildConfig)
    val VERSION_NAME_SIMPLE: String = BuildConfig.VERSION_NAME

    // CI/CD metadata (set these in your pipeline or keep "N/A")
    const val CI_BUILD: String = "N/A"
    const val CI_GIT_BRANCH: String = "main"
    const val CI_GIT_COMMIT_HASH: String = "N/A"

    // FDroid-related (if publishing there, else keep false/null)
    const val FDROID_BUILD: Boolean = false
    const val FDROID_BUILD_VERSION_NAME: String = "N/A"
    const val FDROID_BUILD_VERSION_CODE: Int = -1

    // Project repo (used for AboutActivity and changelog)
    const val REPO_URL: String = "https://github.com/your-username/android-builder"
}
