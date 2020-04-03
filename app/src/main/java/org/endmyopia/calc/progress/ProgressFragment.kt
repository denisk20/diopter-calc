package org.endmyopia.calc.progress

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding
import org.endmyopia.calc.measure.MeasureStateHolder
import org.endmyopia.calc.util.debug
import org.endmyopia.calc.util.getLabelRes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProgressFragment : Fragment() {

    private lateinit var dataBinding: FragmentProgressBinding
    private lateinit var deleteDialogBuilder: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        dataBinding = FragmentProgressBinding.bind(view)
        dataBinding.lifecycleOwner = this
        with(dataBinding) {

            val holder: ProgressStateHolder =
                ViewModelProvider(activity!!).get(ProgressStateHolder::class.java)
            this.holder = holder
            //chart.axisLeft.axisMinimum = yAxisShift
            //chart.axisRight.axisMinimum = yAxisShift
            with(chart) {
                setOnChartValueSelectedListener(object :
                    OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        holder.selectedValue.postValue(null)
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        holder.selectedValue.postValue(e?.data as Measurement?)
                    }
                })
                xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
                xAxis.granularity = 1000 * 60f // 1 min
                description.text = ""

                val yValueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(
                        value: Float
                    ): String {
                        return MeasureStateHolder.formatDiopt.format(value)
                    }
                }
                axisLeft.valueFormatter = yValueFormatter
                axisRight.valueFormatter = yValueFormatter

                val spaceTop = 20f
                axisLeft.spaceTop = spaceTop
                axisRight.spaceTop = spaceTop

                axisLeft.setDrawTopYLabelEntry(false)
                axisRight.setDrawTopYLabelEntry(false)

                xAxis.valueFormatter = object : ValueFormatter() {
                    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)
                    override fun getFormattedValue(
                        value: Float
                    ): String {
                        ViewModelProvider(activity!!).get(ProgressStateHolder::class.java)
                            .minTimestamp.value?.let {
                            return dateFormat.format(Date(it + value.toLong()))
                        }
                        return "n/a"
                    }
                }

            }
            deleteDialogBuilder = AlertDialog.Builder(context!!)

            fillData()

            addFilterOnClickListener(filterLeft, MeasurementMode.LEFT)
            addFilterOnClickListener(filterBoth, MeasurementMode.BOTH)
            addFilterOnClickListener(filterRight, MeasurementMode.RIGHT)

            holder.selectedModes.observe(viewLifecycleOwner, Observer { modes ->
                setDataSetVisible(MeasurementMode.LEFT, modes)
                setDataSetVisible(MeasurementMode.RIGHT, modes)
                setDataSetVisible(MeasurementMode.BOTH, modes)
                dataBinding.chart.invalidate()
            })
            holder.minTimestamp.observe(viewLifecycleOwner, Observer {
                chart.invalidate()
            })

            delete.setOnClickListener {
                holder.selectedValue.value?.distanceMeters?.let {
                    deleteDialogBuilder
                        .setTitle(
                            getString(
                                R.string.delete_measurement,
                                MeasureStateHolder.formatDiopt.format(it)
                            )
                        )
                        .setPositiveButton(
                            R.string.yes
                        ) { _, i ->
                            holder.selectedValue.value?.let { measurement ->
                                GlobalScope.launch {
                                    holder.selectedValue.postValue(null)
                                    AppDatabase.getInstance(context!!.applicationContext as Application)
                                        .getMeasurementDao().deleteById(measurement.id)
                                    val dataSetByLabel = chart.data.getDataSetByLabel(
                                        getString(measurement.mode.getLabelRes()),
                                        false
                                    )
                                    if (dataSetByLabel is LineDataSet) {
                                        chart.highlightValues(null)
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
                                            chart.data.notifyDataChanged()
                                            chart.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }

                        }
                        .setNegativeButton(R.string.no, null)
                        .create().show()
                }
            }
        }

        return view
    }

    private fun setDataSetVisible(mode: MeasurementMode, modes: List<MeasurementMode>) {
        getDataSet(mode)?.isVisible = modes.contains(mode)
    }

    private fun fillData() {
        GlobalScope.launch {
            dataBinding.holder?.let {
                dataBinding.holder?.selectedValue?.postValue(null)
                dataBinding.chart.clear()
//                val measurements =
//                    AppDatabase.getInstance(context!!.applicationContext as Application)
//                        .getMeasurementDao()
//                        .getMeasurements(it.initialModes)
                val measurements = listOf(
                    Measurement(1, MeasurementMode.BOTH, 1582520777860, 0.3),
                    Measurement(2, MeasurementMode.BOTH, 1582520780860, 0.79),
                    Measurement(3, MeasurementMode.BOTH, 1582520785860, 0.20),
                    Measurement(4, MeasurementMode.BOTH, 1582520797860, 0.40),
                    Measurement(5, MeasurementMode.BOTH, 1582520975860, 0.50),

                    Measurement(1, MeasurementMode.LEFT, 1582520776860, 0.0),
                    Measurement(2, MeasurementMode.LEFT, 1582520777860, 0.61),
                    Measurement(3, MeasurementMode.LEFT, 1582520779860, 0.63),
                    Measurement(4, MeasurementMode.LEFT, 1582520796860, 0.60),
                    Measurement(5, MeasurementMode.LEFT, 1582520974860, 0.65),
                    Measurement(5, MeasurementMode.LEFT, 1582520977860, 0.82),

                    Measurement(1, MeasurementMode.RIGHT, 1582520777860, 0.9),
                    Measurement(2, MeasurementMode.RIGHT, 1582520782860, 0.2),
                    Measurement(3, MeasurementMode.RIGHT, 1582520787860, 0.4),
                    Measurement(4, MeasurementMode.RIGHT, 1582520799860, 0.3),
                    Measurement(5, MeasurementMode.RIGHT, 1582520984860, 0.7)
                )
                for (mode in it.initialModes) {
                    createDataSet(measurements, mode)
                }
                dataBinding.chart.notifyDataSetChanged()
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
            }
        }
    }

    private fun createDataSet(
        measurements: List<Measurement>,
        mode: MeasurementMode
    ) {
        val label = getString(mode.getLabelRes())
        val filtered = measurements.filter { m -> m.mode == mode }
        lateinit var values: List<Entry>

        if (filtered.isNotEmpty()) {
            val minTimestamp =
                filtered.reduce { acc, measurement -> if (measurement.date < acc.date) measurement else acc }
                    .date
            ViewModelProvider(activity!!).get(ProgressStateHolder::class.java)
                .minTimestamp.postValue(minTimestamp)
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
            } else {
                val dataSet = LineDataSet(values, label)
                dataSet.lineWidth = 3f
                dataSet.color = when (mode) {
                    MeasurementMode.LEFT -> Color.BLUE
                    MeasurementMode.RIGHT -> Color.RED
                    MeasurementMode.BOTH -> Color.GREEN
                }
                dataSet.circleRadius = 7f
                dataSet.setCircleColor(dataSet.color)

                chart.data.addDataSet(dataSet)
            }
        }
    }

    private fun getDataSet(mode: MeasurementMode): ILineDataSet? {
        with(dataBinding) {
            if (chart.data == null) {
                chart.data = LineData()
            }
            val label = getString(mode.getLabelRes())
            return chart.data.getDataSetByLabel(label, false)
        }
    }

    private fun addFilterOnClickListener(button: ToggleButton, mode: MeasurementMode) {
        button.isChecked = true
        button.setOnCheckedChangeListener { _, checked ->
            val result: ArrayList<MeasurementMode> = ArrayList(
                dataBinding.holder?.selectedModes?.value ?: ArrayList<MeasurementMode>()
            )

            if (checked) {
                result.add(mode)
            } else {
                result.remove(mode)
            }
            dataBinding.holder?.selectedModes?.postValue(result)
        }
    }
}
