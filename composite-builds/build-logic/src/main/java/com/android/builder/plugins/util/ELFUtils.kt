package com.android.builder.plugins.util

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility for working with ELF binaries.
 *
 * Used to validate native libraries like `libaapt2.so`.
 * Rebranded for Android-builder.
 */
object ELFUtils {

    /**
     * Supported ELF ABIs.
     */
    enum class ElfAbi(val machine: Int) {
        ARM(0x28),
        ARM64(0xB7),
        X86_64(0x3E);

        companion object {
            fun forName(name: String): ElfAbi? {
                return when (name) {
                    "armeabi-v7a" -> ARM
                    "arm64-v8a" -> ARM64
                    "x86_64" -> X86_64
                    else -> null
                }
            }
        }
    }

    /**
     * Reads the ELF header from [file] and returns the ABI.
     *
     * @throws IllegalArgumentException if the file is not a valid ELF binary.
     */
    @JvmStatic
    fun getElfAbi(file: File): ElfAbi {
        require(file.exists() && file.isFile) { "ELF file not found: $file" }

        RandomAccessFile(file, "r").use { raf ->
            val header = ByteArray(20)
            raf.readFully(header)

            // Check ELF magic
            if (header[0] != 0x7F.toByte() || header[1] != 'E'.code.toByte()
                || header[2] != 'L'.code.toByte() || header[3] != 'F'.code.toByte()
            ) {
                throw IllegalArgumentException("File is not a valid ELF binary: $file")
            }

            // e_machine field is at offset 18 (2 bytes)
            raf.seek(18)
            val machineBytes = ByteArray(2)
            raf.readFully(machineBytes)

            val machine = ByteBuffer.wrap(machineBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .short
                .toInt()

            return ElfAbi.values().firstOrNull { it.machine == machine }
                ?: throw IllegalArgumentException("Unsupported ELF ABI (machine=$machine) in $file")
        }
    }
}
