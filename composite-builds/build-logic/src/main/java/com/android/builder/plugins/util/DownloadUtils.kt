package com.android.builder.plugins.util

import org.gradle.api.logging.Logger
import java.io.File
import java.net.URL
import java.security.MessageDigest

/**
 * Utility for downloading files with checksum verification.
 *
 * Rebranded for Android-builder.
 */
object DownloadUtils {

    /**
     * Downloads a file from [url] into [file], verifying the SHA-256 [expectedChecksum].
     */
    @JvmStatic
    fun doDownload(file: File, url: String, expectedChecksum: String, logger: Logger) {
        logger.lifecycle("Downloading: $url")
        URL(url).openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val actualChecksum = sha256(file)
        check(actualChecksum == expectedChecksum) {
            "Checksum mismatch for $url. Expected=$expectedChecksum, Got=$actualChecksum"
        }

        logger.lifecycle("Downloaded ${file.name} successfully with verified checksum")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
