package com.android.builder.build.config

/**
 * Utilities for versioning.
 *
 * Rebranded for Android-builder.
 */
object VersionUtils {

    /** Public Sonatype repository for release builds */
    const val SONATYPE_PUBLIC_REPO = "https://s01.oss.sonatype.org/content/groups/public/"

    /** Sonatype snapshots repository for CI builds */
    const val SONATYPE_SNAPSHOTS_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

    /**
     * Returns the normalized version string.
     *
     * @param version version name to normalize
     */
    @JvmStatic
    fun normalizeVersion(version: String): String {
        return version.trim().lowercase()
    }
}
