package org.endmyopia.calc.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference

class NumberPickerPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    var number: Int = INITIAL_VALUE
        get() = getPersistedInt(INITIAL_VALUE)
        set(value) {
            field = value
            persistInt(value)
        }

    override fun onSetInitialValue(defaultValue: Any?) {
        number = if (defaultValue is Int) {
            persistInt(defaultValue)
            defaultValue
        } else {
            persistInt(INITIAL_VALUE)
            INITIAL_VALUE
        }
    }


    companion object {
        // allowed range
        const val INITIAL_VALUE = 24
        const val MAX_VALUE = 100
        const val MIN_VALUE = 0

        // enable or disable the 'circular behavior'
        const val WRAP_SELECTOR_WHEEL = true
    }
}