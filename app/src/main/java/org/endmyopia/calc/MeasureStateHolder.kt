package org.endmyopia.calc

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DecimalFormat

/**
 * @author denisk
 * @since 2019-04-29.
 */
class MeasureStateHolder : ViewModel() {
    private val formatDist = DecimalFormat("#.0 cm")
    private val formatDiopt = DecimalFormat("-#.0 dpt")

    val distanceVal: MutableLiveData<Double> by lazy {
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

    val orientation: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun update(dist: Double, diopts: Double) {
        distanceVal.postValue(dist)
        dioptersVal.postValue(diopts)

        distanceStr.postValue(formatDist.format(dist))
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
        fun setMarginTop(textView: TextView, value: Float) {
            val layoutParams: ViewGroup.MarginLayoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = value.toInt()
        }
    }
}
