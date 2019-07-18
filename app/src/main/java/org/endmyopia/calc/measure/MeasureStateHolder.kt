package org.endmyopia.calc.measure

import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.endmyopia.calc.R
import org.endmyopia.calc.data.MeasurementMode
import java.text.DecimalFormat


/**
 * @author denisk
 * @since 2019-04-29.
 */
class MeasureStateHolder(private val app: Application) : AndroidViewModel(app) {
    val EYES_LOW = 76
    val EYES_HIGH = 128

    private val formatDist = DecimalFormat("#.0 cm")
    private val formatDiopt = DecimalFormat("-#.00 dpt")

    val distanceMetersVal: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val dioptersVal: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val distanceStr: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val dioptersStr: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val hasTakenMeasurement: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val mode: MutableLiveData<MeasurementMode> by lazy {
        MutableLiveData<MeasurementMode>(MeasurementMode.BOTH)
    }

    var lastPersistedMeasurementId = 0L

    fun eyesString(): String {
        return app.resources.getString(
            when (mode.value) {
                MeasurementMode.LEFT -> R.string.left_eye
                MeasurementMode.RIGHT -> R.string.right_eye
                MeasurementMode.BOTH -> R.string.both_eyes
                null -> throw IllegalStateException("no mode?")
            }
        )
    }

    fun isPortrait(): Boolean {
        return app.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun update(distMeters: Double) {
        val diopts = 1 / distMeters

        distanceMetersVal.postValue(distMeters)
        dioptersVal.postValue(diopts)

        distanceStr.postValue(formatDist.format(distMeters * 100))
        dioptersStr.postValue(formatDiopt.format(diopts))
    }

    object CommonBindingUtil {
        @JvmStatic
        @BindingAdapter("backgroundTint")
        fun setBackgroundTint(fab: FloatingActionButton, tint: Int) {
            fab.backgroundTintList = ColorStateList.valueOf(tint)
        }

        @JvmStatic
        @BindingAdapter("srcCompat")
        fun setSrcCompat(fab: FloatingActionButton, drawable: Drawable) {
            fab.setImageDrawable(drawable)
        }

        @JvmStatic
        @BindingAdapter("android:textSize")
        fun setTextSize(textView: TextView, textSize: Float) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        }

        @JvmStatic
        @BindingAdapter("android:layout_marginTop")
        fun setMarginTop(view: View, value: Float) {
            val layoutParams: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = value.toInt()
        }

        @JvmStatic
        @BindingAdapter("android:layout_marginBottom")
        fun setMarginBottom(view: View, valueDp: Float) {
            val layoutParams: ViewGroup.MarginLayoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = dpToPx(
                valueDp,
                view.context
            ).toInt()
        }

        @JvmStatic
        @BindingAdapter("fabEnabled")
        fun fabEnabled(view: FloatingActionButton, enabled: Boolean) {
            view.isEnabled = enabled
        }
        private fun dpToPx(dp: Float, context: Context) =
            dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}
