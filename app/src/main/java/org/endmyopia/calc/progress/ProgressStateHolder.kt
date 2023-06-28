package org.endmyopia.calc.progress

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.measure.MeasureStateHolder
import org.endmyopia.calc.util.dpt
import org.endmyopia.calc.util.getEyesText

/**
 * @author denisk
 * @since 2019-07-20.
 */
class ProgressStateHolder : ViewModel() {
    val data: MutableLiveData<List<Measurement>> by lazy {
        MutableLiveData<List<Measurement>>(listOf())
    }
    val selectedModes: MutableLiveData<List<MeasurementMode>> by lazy {
        MutableLiveData<List<MeasurementMode>>(
            initialModes
        )
    }
    val selectedValue: MutableLiveData<Measurement> by lazy {
        MutableLiveData<Measurement>()
    }

    fun fillData(context: Context) {
        GlobalScope.launch {
            selectedValue.postValue(null)
//            val measurements =
//                selectedModes.value?.let {
//                    AppDatabase.getInstance(context.applicationContext as Application)
//                        .getMeasurementDao()
//                        .getMeasurements(it)
//                }
            val measurements = listOf(
                Measurement(1, MeasurementMode.BOTH, 1582520777860, 0.3),
                Measurement(2, MeasurementMode.BOTH, 1582520780860, 0.79),
                Measurement(3, MeasurementMode.BOTH, 1582520785860, 0.20),
                Measurement(4, MeasurementMode.BOTH, 1582520797860, 0.40),
                Measurement(5, MeasurementMode.BOTH, 1582520975860, 0.50),

                Measurement(1, MeasurementMode.LEFT, 1582520770860, 0.5),
                Measurement(2, MeasurementMode.LEFT, 1582520772860, 0.61),
                Measurement(3, MeasurementMode.LEFT, 1582520773860, 0.63),
                Measurement(4, MeasurementMode.LEFT, 1582520774860, 0.60),
                Measurement(5, MeasurementMode.LEFT, 1582520775860, 0.65),
                Measurement(5, MeasurementMode.LEFT, 1582520777860, 0.82),

                Measurement(1, MeasurementMode.RIGHT, 1582520777860, 0.9),
                Measurement(2, MeasurementMode.RIGHT, 1582520782860, 0.2),
                Measurement(3, MeasurementMode.RIGHT, 1582520787860, 0.4),
                Measurement(4, MeasurementMode.RIGHT, 1582520799860, 0.3),
                Measurement(5, MeasurementMode.RIGHT, 1582520984860, 0.7)
            )

            data.postValue(measurements)
        }
    }

    fun showDeleteDialog(context: Context, measurement: Measurement) {
        val deleteDialogBuilder = AlertDialog.Builder(context)
        deleteDialogBuilder
            .setTitle(
                context.getString(
                    R.string.delete_measurement,
                    MeasureStateHolder.formatDiopt.format(dpt(measurement.distanceMeters)),
                    getEyesText(measurement.mode, context)
                )
            )
            .setPositiveButton(
                    R.string.yes
                ) { _, i ->
                    selectedValue.value?.let { measurement ->
                        GlobalScope.launch {
                            selectedValue.postValue(null)
                            AppDatabase.getInstance(context.applicationContext as Application)
                                .getMeasurementDao().deleteById(measurement.id)
                            fillData(context)
                        }
                    }

                }
                .setNegativeButton(R.string.no) { _, i -> selectedValue.postValue(null) }
                .create().show()

    }

    companion object {
        val initialModes = listOf(
            MeasurementMode.LEFT,
            MeasurementMode.BOTH,
            MeasurementMode.RIGHT
        )
    }
}