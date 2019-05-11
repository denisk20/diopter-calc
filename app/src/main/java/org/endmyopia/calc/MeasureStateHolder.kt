package org.endmyopia.calc

import android.content.res.ColorStateList
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
    private val formatDiopt = DecimalFormat("-#.0 diopt")

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

    object CommonBindingUtil {
        @JvmStatic
        @BindingAdapter("backgroundTint")
        fun setBackgroundTint(fab: FloatingActionButton, tint: Int) {
            fab.backgroundTintList = ColorStateList.valueOf(tint)
        }
    }



    fun update(dist: Double, diopts: Double) {
        distanceVal.postValue(dist)
        dioptersVal.postValue(diopts)

        distanceStr.postValue(formatDist.format(dist))
        dioptersStr.postValue(formatDiopt.format(diopts))
    }
}
