package org.endmyopia.calc.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import org.endmyopia.calc.R

class NumberPickerPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    override fun getSummary(): CharSequence {
        return getPersistedInt(INITIAL_VALUE).toString() + context.getString(R.string.pt)
    }

    fun getPersistedInt() = super.getPersistedInt(INITIAL_VALUE)

    fun doPersistInt(value: Int) {
        super.persistInt(value)
        notifyChanged()
    }

    companion object {
        // allowed range
        const val INITIAL_VALUE = 20
        const val MIN_VALUE = 12
        const val MAX_VALUE = 100
    }
}