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
        return context.resources.getColor(
            when (getFocusStyleOrdinal()) {
                0 -> R.color.white
                1 -> R.color.black
                2 -> R.color.green
                else -> R.color.white
            }
            , null
        )
    }

    @Bindable
    fun getBackgroundColor(): Int {
        return context.resources.getColor(
            when (getFocusStyleOrdinal()) {
                0 -> R.color.black
                1 -> R.color.white
                2 -> R.color.white
                else -> R.color.white
            }
            , null
        )
    }

    fun getFocusStyleOrdinal(): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(FOCUS_STYLE_KEY, 0)

    companion object {
        const val FOCUS_STYLE_KEY = "focus_style"
    }
}