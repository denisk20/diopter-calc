package org.endmyopia.calc.progress

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.endmyopia.calc.data.MeasurementMode

/**
 * @author denisk
 * @since 2019-07-20.
 */
class ProgressStateHolder : ViewModel() {
    val selectedModes: MutableLiveData<List<MeasurementMode>> by lazy {
        MutableLiveData<List<MeasurementMode>>(
            arrayListOf(
                MeasurementMode.LEFT,
                MeasurementMode.BOTH,
                MeasurementMode.RIGHT
            )
        )
    }

    fun filterEnabled(mode: MeasurementMode): Boolean {
        return selectedModes.value?.contains(mode) == true
    }
}