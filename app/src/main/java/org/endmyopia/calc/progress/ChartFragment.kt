package org.endmyopia.calc.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.endmyopia.calc.R
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.util.dpt
import org.endmyopia.calc.util.getLabelRes

/**
 * @author denisk
 * @since 01.01.2022.
 */
class ChartFragment : Fragment() {

    lateinit var chart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val holder: ProgressStateHolder =
            ViewModelProvider(requireActivity()).get(ProgressStateHolder::class.java)

        val chart: LineChart =
            inflater.inflate(R.layout.fragment_chart, container, false) as LineChart

        this.chart = chart

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
            xAxis.isEnabled = false

            description.text = ""

            val yValueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(
                    value: Float
                ): String {
                    return org.endmyopia.calc.measure.MeasureStateHolder.formatDiopt.format(value)
                }
            }
            axisLeft.valueFormatter = yValueFormatter
            axisRight.valueFormatter = yValueFormatter

            val spaceTop = 20f
            axisLeft.spaceTop = spaceTop
            axisRight.spaceTop = spaceTop

            axisLeft.setDrawTopYLabelEntry(false)
            axisRight.setDrawTopYLabelEntry(false)

            val markerView = ProgressMarkerView(
                context,
                org.endmyopia.calc.R.layout.progress_popup
            )
            markerView.chartView = chart
            chart.marker = markerView

            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
        }

        holder.selectedModes.observe(viewLifecycleOwner, { modes ->
            setDataSetVisible(MeasurementMode.LEFT, modes)
            setDataSetVisible(MeasurementMode.RIGHT, modes)
            setDataSetVisible(MeasurementMode.BOTH, modes)
            chart.invalidate()
            Thread.sleep(300)
        })

        holder.data.observe(viewLifecycleOwner, { measurements ->
            if (measurements.isNotEmpty()) {
                val minTimestamp =
                    measurements.reduce { acc, measurement -> if (measurement.date < acc.date) measurement else acc }
                        .date
                val maxTimestamp =
                    measurements.reduce { acc, measurement -> if (measurement.date > acc.date) measurement else acc }
                        .date

                chart.clear()
                for (mode in ProgressStateHolder.initialModes) {
                    createDataSet(measurements, mode, minTimestamp, maxTimestamp)
                }
            }
            chart.notifyDataSetChanged()

        })
        return chart
    }

    private fun setDataSetVisible(mode: MeasurementMode, modes: List<MeasurementMode>) {
        getDataSet(mode)?.isVisible = modes.contains(mode)
    }


    private fun createDataSet(
        measurements: List<Measurement>,
        mode: MeasurementMode,
        minTimestamp: Long,
        maxTimestamp: Long
    ) {
        val label = getString(mode.getLabelRes())
        val filtered = measurements.filter { m -> m.mode == mode }
        lateinit var values: List<Entry>

        if (filtered.isNotEmpty()) {
            values = filtered
                .map { m ->
                    Entry(
                        if (minTimestamp == maxTimestamp) 0f else ((m.date - minTimestamp).toFloat() / (maxTimestamp - minTimestamp)),
                        dpt(m.distanceMeters).toFloat(),
                        m
                    )
                }
        } else {
            values = emptyList()
        }
        with(chart) {
            if (data == null) {
                data = LineData()
            }
            val dataSetByLabel = data.getDataSetByLabel(label, false)
            if (dataSetByLabel is LineDataSet) {
                dataSetByLabel.values = values
            } else {
                val dataSet = LineDataSet(values, label)
                dataSet.lineWidth = 3f
                dataSet.color = when (mode) {
                    MeasurementMode.LEFT -> android.graphics.Color.BLUE
                    MeasurementMode.RIGHT -> android.graphics.Color.RED
                    MeasurementMode.BOTH -> android.graphics.Color.GREEN
                }
                dataSet.circleRadius = 7f
                dataSet.setCircleColor(dataSet.color)

                data.addDataSet(dataSet)
            }
        }
    }

    private fun getDataSet(mode: MeasurementMode): ILineDataSet? {
        with(chart) {
            if (data == null) {
                data = LineData()
            }
            val label = getString(mode.getLabelRes())
            return data.getDataSetByLabel(label, false)
        }
    }

}