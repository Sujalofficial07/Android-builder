package com.android.builder.activities.editor

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Background thread that reads logcat output.
 */
class BuilderLogcatReader : Thread() {

    private var running = true
    private var process: Process? = null

    override fun run() {
        try {
            process = Runtime.getRuntime().exec("logcat")
            val reader = BufferedReader(InputStreamReader(process!!.inputStream))

            var line: String?
            while (running && reader.readLine().also { line = it } != null) {
                println("[BuilderLogcat] $line")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stopReader()
        }
    }

    fun stopReader() {
        running = false
        process?.destroy()
        process = null
    }
}
