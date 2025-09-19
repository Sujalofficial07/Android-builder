package com.android.builder.plugins.tasks

import com.android.builder.build.config.FDroidConfig
import com.android.builder.build.config.isFDroidBuild
import com.android.builder.plugins.util.DownloadUtils
import com.android.builder.plugins.util.ELFUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task to prepare the AAPT2 binary for Android-builder.
 *
 * - For F-Droid builds: copies prebuilt binaries.
 * - For normal builds: downloads binaries from GitHub releases.
 */
abstract class SetupAapt2Task : DefaultTask() {

    /**
     * The output directory.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    companion object {
        private val AAPT2_CHECKSUMS = mapOf(
            "arm64-v8a" to "be2cea61814678f7a9e61bf818a6666e6097a7d67d6c19498a4d7aa690bc4151",
            "armeabi-v7a" to "ba3413c680933dffd3c3d35da8d450c474ff5ccab95c4b9db28841c53b7a3cdf",
            "x86_64" to "4861171c1efcffe41f4466937e6a392b243ffb014813b4e60f0b77bb46ab254d"
        )

        private const val DEFAULT_VERSION = "34.0.4"
        private const val AAPT2_DOWNLOAD_URL =
            "https://github.com/AndroidBuilderOfficial/platform-tools/releases/download/v%1\$s/aapt2-%2\$s"
    }

    @TaskAction
    fun setupAapt2() {
        // F-Droid builds use pre-specified binaries
        if (project.isFDroidBuild) {
            val arch = FDroidConfig.fDroidBuildArch!!

            val file = outputDirectory.file("${arch}/libaapt2.so").get().asFile
            file.parentFile.deleteRecursively()
            file.parentFile.mkdirs()

            val aapt2File = requireNotNull(FDroidConfig.aapt2Files[arch]) {
                "F-Droid build is enabled but path to AAPT2 file for $arch is not set."
            }

            val aapt2 = File(aapt2File)

            require(aapt2.exists() && aapt2.isFile) {
                "F-Droid AAPT2 file does not exist or is not a file: $aapt2"
            }

            logger.info("Copying $aapt2 to $file")
            aapt2.copyTo(file, overwrite = true)
            assertAapt2Arch(file, ELFUtils.ElfAbi.forName(arch)!!)
            return
        }

        // Non-F-Droid builds: download AAPT2 binaries
        AAPT2_CHECKSUMS.forEach { (arch, checksum) ->
            val file = outputDirectory.file("${arch}/libaapt2.so").get().asFile
            file.parentFile.deleteRecursively()
            file.parentFile.mkdirs()

            val remoteUrl = AAPT2_DOWNLOAD_URL.format(DEFAULT_VERSION, arch)
            DownloadUtils.doDownload(file, remoteUrl, checksum, logger)
            assertAapt2Arch(file, ELFUtils.ElfAbi.forName(arch)!!)
        }
    }

    private fun assertAapt2Arch(aapt2: File, elfAbi: ELFUtils.ElfAbi) {
        val fileAbi = ELFUtils.getElfAbi(aapt2)
        check(fileAbi == elfAbi) {
            "Mismatched ABI for aapt2 binary. Required $elfAbi but found $fileAbi"
        }
    }
}
