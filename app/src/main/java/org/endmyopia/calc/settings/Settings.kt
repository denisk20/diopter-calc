package org.endmyopia.calc.settings

import android.content.SharedPreferences

class Settings(private val preferences: SharedPreferences) {
    val measureWithGesture by lazy {
        preferences.getBoolean("measure_with_gesture", true)
    }
}