package org.endmyopia.calc.progress

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode

/**
 * @author denisk
 * @since 2019-07-20.
 */
class ProgressStateHolder : ViewModel() {
    val selectedModes: MutableLiveData<List<MeasurementMode>> by lazy {
        MutableLiveData<List<MeasurementMode>>(
            initialModes
        )
    }
    val selectedValue: MutableLiveData<Measurement> by lazy {
        MutableLiveData<Measurement>()
    }

    val minTimestamp: MutableLiveData<Long> by lazy {
        MutableLiveData(0L)
    }

    companion object {
        val initialModes = listOf(
            MeasurementMode.LEFT,
            MeasurementMode.BOTH,
            MeasurementMode.RIGHT
        )
    }
}