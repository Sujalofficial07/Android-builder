package com.android.builder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.android.builder.R

/**
 * Activity to handle and display crash reports in Android-builder.
 */
class CrashHandlerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_handler)

        val trace = intent.getStringExtra(TRACE_KEY) ?: "No stack trace available"
        val traceView = findViewById<TextView>(R.id.crash_trace)
        traceView.text = trace
    }

    companion object {
        const val REPORT_ACTION = "com.android.builder.intent.action.REPORT_CRASH"
        const val TRACE_KEY = "trace"
    }
}
