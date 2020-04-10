package org.endmyopia.calc.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.preference.PreferenceDialogFragmentCompat


/**
 * @author denisk
 * @since 4/8/20.
 */
class NumberPickerPreferenceDialog : PreferenceDialogFragmentCompat() {
    var number: Int = 24
    lateinit var numberPicker: NumberPicker

    override fun onCreateDialogView(context: Context?): View {
        numberPicker = NumberPicker(context)
        numberPicker.minValue = NumberPickerPreference.MIN_VALUE
        numberPicker.maxValue = NumberPickerPreference.MAX_VALUE

        return numberPicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        numberPicker.value = (preference as NumberPickerPreference).getPersistedInt()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            numberPicker.clearFocus()
            val newValue: Int = numberPicker.value
            if (preference.callChangeListener(newValue)) {
                (preference as NumberPickerPreference).doPersistInt(newValue)
                preference.summary
            }
        }
    }

    companion object {
        fun newInstance(key: String): NumberPickerPreferenceDialog {
            val fragment = NumberPickerPreferenceDialog()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle

            return fragment
        }
    }
}