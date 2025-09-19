package com.android.builder.plugins.util

import java.io.File
import java.util.Properties

/**
 * Utility for loading signing keys for release builds.
 *
 * Rebranded for Android-builder.
 */
object SigningKeyUtils {

    /**
     * Loads a `keystore.properties` file if present.
     *
     * @param filePath Path to the keystore properties file
     * @return Properties loaded, or empty if file does not exist
     */
    @JvmStatic
    fun loadKeystoreProperties(filePath: String): Properties {
        val props = Properties()
        val file = File(filePath)

        if (!file.exists() || !file.isFile) {
            return props
        }

        file.inputStream().use { input ->
            props.load(input)
        }

        return props
    }
}
