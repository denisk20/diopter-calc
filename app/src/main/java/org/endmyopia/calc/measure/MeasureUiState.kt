package org.endmyopia.calc.measure

import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.preference.PreferenceManager
import org.endmyopia.calc.R

/**
 * @author denisk
 * @since 4/14/20.
 */
class MeasureUiState(val context: Context) : BaseObservable() {

    @Bindable
    fun getFontColor(): Int {
        val ordinal =
            PreferenceManager.getDefaultSharedPreferences(context).getInt("focus_style", 0)
        return context.resources.getColor(
            when (ordinal) {
                0 -> R.color.white
                1 -> R.color.black
                2 -> R.color.green
                else -> R.color.white
            }
            , null
        )
    }
}