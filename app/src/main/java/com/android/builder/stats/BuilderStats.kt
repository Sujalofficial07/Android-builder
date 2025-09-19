package com.android.builder.stats

import androidx.work.Data

/**
 * Collects anonymized usage statistics for Android-builder.
 */
object BuilderStats {

    private val collectedData: MutableMap<String, Any> = mutableMapOf()

    val statData: Map<String, Any>
        get() = collectedData.toMap()

    fun put(key: String, value: Any) {
        collectedData[key] = value
    }

    fun toInputData(): Data {
        val builder = Data.Builder()
        collectedData.forEach { (key, value) ->
            when (value) {
                is Int -> builder.putInt(key, value)
                is Long -> builder.putLong(key, value)
                is Boolean -> builder.putBoolean(key, value)
                is Float -> builder.putFloat(key, value)
                is Double -> builder.putDouble(key, value)
                is String -> builder.putString(key, value)
            }
        }
        return builder.build()
    }
}
