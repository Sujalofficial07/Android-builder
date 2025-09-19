package com.android.builder.utils

import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.android.builder.BuildConfig
import com.android.builder.app.BuilderApplication
import com.android.builder.buildinfo.BuildInfo
import com.termux.shared.android.PackageUtils
import com.termux.shared.termux.TermuxConstants
import com.termux.shared.termux.TermuxUtils

/**
 * Utility class for collecting and formatting build-related info.
 *
 * Rebranded for Android-builder.
 */
object BuildInfoUtils {

    private val BUILD_INFO_HEADER by lazy {
        val map = mapOf(
            "Version" to "v${BuildInfo.VERSION_NAME_SIMPLE} (${AppUtils.getAppVersionCode()})",
            "CI Build" to BuildInfo.CI_BUILD,
            "Branch" to BuildInfo.CI_GIT_BRANCH,
            "Commit" to BuildInfo.CI_GIT_COMMIT_HASH,
            "Variant" to "${BuilderApplication.cpuAbiName} (${BuildConfig.BUILD_TYPE})",
            "Build type" to getBuildType(),
            "F-Droid Build" to BuildInfo.FDROID_BUILD,
            "F-Droid Version" to BuildInfo.FDROID_BUILD_VERSION_NAME,
            "F-Droid Version code" to BuildInfo.FDROID_BUILD_VERSION_CODE,
            "SDK Version" to Build.VERSION.SDK_INT,
            "Supported ABIs" to "[${Build.SUPPORTED_ABIS.joinToString(", ")}]",
            "Manufacturer" to DeviceUtils.getManufacturer(),
            "Device" to DeviceUtils.getModel(),
        )
        map.entries.joinToString(separator = System.lineSeparator()) { "${it.key} : ${it.value}" }
            .trim()
    }

    @JvmStatic
    fun getBuildInfoHeader(): String = BUILD_INFO_HEADER

    private fun getBuildType() = getBuildType(BuilderApplication.instance)

    fun getBuildType(context: Context) =
        if (isOfficialBuild(context)) "OFFICIAL" else "UNOFFICIAL"

    /**
     * Whether the Android-builder build is official or not.
     * Checks the APK signature digest against the official signing key.
     */
    @JvmStatic
    fun isOfficialBuild(context: Context): Boolean {
        val sha256DigestForPackage =
            PackageUtils.getSigningCertificateSHA256DigestForPackage(
                context,
                TermuxConstants.TERMUX_PACKAGE_NAME
            )

        val signer = TermuxUtils.getAPKRelease(sha256DigestForPackage)

        return TermuxConstants.APK_RELEASE_ANDROIDIDE == signer ||
               TermuxConstants.APK_RELEASE_FDROID == signer
    }
}
