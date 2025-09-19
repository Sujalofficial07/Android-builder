package com.android.builder.buildinfo

/**
 * Build metadata for Android-builder.
 *
 * Replaces AndroidIDE BuildInfo.
 */
object BuildInfo {

    /** Simple version name (e.g., "1.2.3") */
    const val VERSION_NAME_SIMPLE: String = "1.0.0"

    /** CI build flag */
    const val CI_BUILD: Boolean = false

    /** Git branch name of the CI build */
    const val CI_GIT_BRANCH: String = "main"

    /** Git commit hash of the CI build */
    const val CI_GIT_COMMIT_HASH: String = "unknown"

    /** Whether this is an F-Droid build */
    const val FDROID_BUILD: Boolean = false

    /** F-Droid version name */
    const val FDROID_BUILD_VERSION_NAME: String = ""

    /** F-Droid version code */
    const val FDROID_BUILD_VERSION_CODE: Int = -1

    /** Upstream repository URL */
    const val REPO_URL: String = "https://github.com/AndroidBuilderOfficial/android-builder"
}
