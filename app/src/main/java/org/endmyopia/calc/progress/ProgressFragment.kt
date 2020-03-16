package org.endmyopia.calc.progress

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding
import org.endmyopia.calc.util.debug
import org.endmyopia.calc.util.getLabelRes


class ProgressFragment : Fragment() {

    private lateinit var dataBinding: FragmentProgressBinding
    private val yAxisShift = -0.1f
    private lateinit var deleteDialogBuilder: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        dataBinding = FragmentProgressBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: ProgressStateHolder =
            ViewModelProvider(activity!!).get(ProgressStateHolder::class.java)
        dataBinding.holder = holder

        //dataBinding.chart.axisLeft.axisMinimum = yAxisShift
        //dataBinding.chart.axisRight.axisMinimum = yAxisShift
        dataBinding.chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                holder.selectedValue.postValue(null)
            }

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                holder.selectedValue.postValue(e?.data as Measurement?)
            }
        })

        deleteDialogBuilder = AlertDialog.Builder(context!!)

        addFilterOnClickListener(dataBinding.filterLeft, MeasurementMode.LEFT)
        addFilterOnClickListener(dataBinding.filterBoth, MeasurementMode.BOTH)
        addFilterOnClickListener(dataBinding.filterRight, MeasurementMode.RIGHT)

        holder.selectedModes.observe(viewLifecycleOwner, Observer {
            processFilterButtonChange(it, dataBinding.filterLeft, MeasurementMode.LEFT)
            processFilterButtonChange(it, dataBinding.filterBoth, MeasurementMode.BOTH)
            processFilterButtonChange(it, dataBinding.filterRight, MeasurementMode.RIGHT)

            fillData(it)
        })

        dataBinding.delete.setOnClickListener {
            deleteDialogBuilder
                .setTitle(
                    getString(
                        R.string.delete_measurement,
                        dataBinding.holder?.selectedValue?.value?.distanceMeters.toString()
                    )
                )
                .setPositiveButton(
                    R.string.yes
                ) { dialogInterface, i ->
                    dataBinding.holder?.selectedValue?.value?.let { measurement ->
                        GlobalScope.launch {
                            dataBinding.holder?.selectedValue?.postValue(null)
                            AppDatabase.getInstance(context!!.applicationContext as Application)
                                .getMeasurementDao().deleteById(measurement.id)
                            val dataSetByLabel = dataBinding.chart.data.getDataSetByLabel(
                                getString(measurement.mode.getLabelRes()),
                                false
                            )
                            if (dataSetByLabel is LineDataSet) {
                                dataBinding.chart.highlightValues(null)
                                var index = -1
                                for (i in 0 until dataSetByLabel.entryCount) {
                                    debug("$i : ${dataSetByLabel.getEntryForIndex(i).data}")
                                    if ((dataSetByLabel.getEntryForIndex(i).data as Measurement).id == measurement.id) {
                                        index = i
                                        break
                                    }
                                }
                                if (index > -1) {
                                    debug(
                                        "removed ${measurement.distanceMeters}? -- ${dataSetByLabel.removeEntry(
                                            index
                                        )}"
                                    )
                                    dataSetByLabel.notifyDataSetChanged()
                                    dataBinding.chart.data.notifyDataChanged()
                                    dataBinding.chart.notifyDataSetChanged()
                                }

                            }
                        }
                    }

                }
                .setNegativeButton(R.string.no, null)
                .create().show()
        }

        return view
    }

    private fun fillData(modes: List<MeasurementMode>) {
        GlobalScope.launch {
            dataBinding.holder?.selectedValue?.postValue(null)
            dataBinding.chart.highlightValues(null)
            val measurements =
                AppDatabase.getInstance(context!!.applicationContext as Application)
                    .getMeasurementDao()
                    .getMeasurements(modes)
            debug(measurements.toString())

            with(modes) {
                if (contains(MeasurementMode.RIGHT)) createDataSet(
                    measurements,
                    MeasurementMode.RIGHT
                ) else removeDataSet(MeasurementMode.RIGHT)

                if (contains(MeasurementMode.BOTH)) createDataSet(
                    measurements,
                    MeasurementMode.BOTH
                ) else removeDataSet(MeasurementMode.BOTH)

                if (contains(MeasurementMode.LEFT)) createDataSet(
                    measurements,
                    MeasurementMode.LEFT
                ) else removeDataSet(MeasurementMode.LEFT)
            }
        }
    }

    private fun removeDataSet(
        mode: MeasurementMode
    ) {
        with(dataBinding) {
            if (chart.data == null) {
                chart.data = LineData()
            }

            val label = getString(mode.getLabelRes())
            val dataSet = chart.data.getDataSetByLabel(label, false)
            if (dataSet != null) {
                chart.data.removeDataSet(dataSet)
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            }
        }
    }

    private fun createDataSet(
        measurements: List<Measurement>,
        mode: MeasurementMode
    ) {
        val label = getString(mode.getLabelRes())
//        val values = listOf(
//            Entry(Date(1582520774860).time.toFloat(), 0.62f),
//            Entry(Date(1582520775860).time.toFloat(), 0.63f),
//            Entry(Date(1582520778860).time.toFloat(), 0.60f)
//        )
//        val fakeMeasurements = listOf<Measurement>(
//            Measurement(1, MeasurementMode.BOTH, 1582520776860, 0.0),
//            Measurement(2, MeasurementMode.BOTH, 1582520777860, 0.61),
//            Measurement(3, MeasurementMode.BOTH, 1582520779860, 0.63),
//            Measurement(4, MeasurementMode.BOTH, 1582520796860, 0.60),
//            Measurement(5, MeasurementMode.BOTH, 1582520874860, 0.65)
//        )
        val filtered = measurements.filter { m -> m.mode == mode }
        lateinit var values: List<Entry>

        if (filtered.isNotEmpty()) {
            val minTimestamp =
                filtered.reduce { acc, measurement -> if (measurement.date < acc.date) measurement else acc }
                    .date
            values = filtered
                .map { m ->
                    Entry(
                        (m.date - minTimestamp).toFloat(),
                        m.distanceMeters.toFloat(),
                        m
                    )
                }
        } else {
            values = emptyList()
        }
        with(dataBinding) {
            if (chart.data == null) {
                chart.data = LineData()
                this@ProgressFragment.context?.let {
                    val markerView = ProgressMarkerView(
                        it,
                        R.layout.progress_popup
                    )
                    markerView.chartView = chart
                    chart.marker = markerView
                }
            }
            val dataSetByLabel = chart.data.getDataSetByLabel(label, false)
            if (dataSetByLabel is LineDataSet) {
                dataSetByLabel.values = values
                dataSetByLabel.notifyDataSetChanged()
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                val dataSet = LineDataSet(values, label)
                dataSet.circleRadius = 10f

                chart.data.addDataSet(dataSet)
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            }
        }
    }

    private fun processFilterButtonChange(
        selectedModes: List<MeasurementMode>,
        button: Button,
        mode: MeasurementMode
    ) {
        button.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context!!,
                if (selectedModes.contains(mode)) R.color.gray else R.color.white
            )
        )
    }

    private fun addFilterOnClickListener(button: Button, mode: MeasurementMode) {
        button.setOnClickListener {
            val result: ArrayList<MeasurementMode> = ArrayList<MeasurementMode>(
                dataBinding.holder?.selectedModes?.value ?: ArrayList<MeasurementMode>()
            )

            if (result.contains(mode)) {
                result.remove(mode)
            } else {
                result.add(mode)
            }
            dataBinding.holder?.selectedModes?.postValue(result)
        }
    }
}
