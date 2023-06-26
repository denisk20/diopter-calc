package org.endmyopia.calc.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.endmyopia.calc.R
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentChartBinding
import org.endmyopia.calc.util.dpt
import org.endmyopia.calc.util.getLabelRes
import java.util.AbstractMap.SimpleEntry
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @author denisk
 * @since 01.01.2022.
 */
class ChartFragment : Fragment() {

    val maxPoints = 5

    private lateinit var dataBinding: FragmentChartBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val holder: ProgressStateHolder =
            ViewModelProvider(requireActivity()).get(ProgressStateHolder::class.java)

        val view =
            inflater.inflate(R.layout.fragment_chart, container, false)

        dataBinding = FragmentChartBinding.bind(view)
        dataBinding.lifecycleOwner = this
        dataBinding.holder = holder

        with(dataBinding.chart) {

        }

        holder.selectedModes.observe(viewLifecycleOwner) { modes ->
            setDataSetVisible(MeasurementMode.LEFT, modes)
            setDataSetVisible(MeasurementMode.RIGHT, modes)
            setDataSetVisible(MeasurementMode.BOTH, modes)
            dataBinding.chart.invalidate()
            Thread.sleep(300)
        }

        holder.data.observe(viewLifecycleOwner) { measurements ->
            if (measurements.isNotEmpty()) {
                var minTimestamp = Long.MAX_VALUE
                var maxTimestamp = 0L

                measurements.forEach {
                    if (minTimestamp > it.date) minTimestamp = it.date
                    if (maxTimestamp < it.date) maxTimestamp = it.date
                }
                val map = measurements?.groupBy { it.mode }

                //dataBinding.chart.clear()
                for (mode in ProgressStateHolder.initialModes) {
                    val measurementsForMode = map?.getOrDefault(mode, listOf()).orEmpty()
                    var meas = measurementsForMode
                    if (measurementsForMode.size > maxPoints) {
                        val groupSize = ceil(measurementsForMode.size / maxPoints.toFloat()).toInt()
                        val groupsCount =
                            floor(measurementsForMode.size / groupSize.toFloat()).toInt()
                        val avgMeasurements = mutableListOf<Measurement>()
                        for (i in measurementsForMode.indices step groupSize) {
                            val subList = measurementsForMode.subList(i, i + groupSize)
                            val avgDate = subList.map { it.date }.average().toLong()
                            val avgDistance = subList.map { it.distanceMeters }.average()
                            val measurement = Measurement(0, mode, avgDate, avgDistance, 0.0)
                            avgMeasurements.add(measurement)
                        }
                        for (i in groupSize * groupsCount until measurementsForMode.size) {
                            avgMeasurements.add(measurementsForMode[i])
                        }
                        meas = avgMeasurements
                    }
                    createDataSet(meas, mode, minTimestamp, maxTimestamp)
                }
            } else {
                //dataBinding.chart.clear()
            }
            //dataBinding.chart.notifyDataSetChanged()
        }

        return view
    }

    private fun setDataSetVisible(mode: MeasurementMode, modes: List<MeasurementMode>) {
        //getDataSet(mode)?.isVisible = modes.contains(mode)
    }


    private fun createDataSet(
        measurements: List<Measurement>,
        mode: MeasurementMode,
        minTimestamp: Long,
        maxTimestamp: Long
    ) {
        val label = getString(mode.getLabelRes())
        val filtered = measurements.reversed()

        var values: List<SimpleEntry<Float, Float>> = if (filtered.isNotEmpty()) {
            filtered
                .map { m ->
                    SimpleEntry<Float, Float>(
                        if (minTimestamp == maxTimestamp) 0f else ((m.date - minTimestamp).toFloat() / (maxTimestamp - minTimestamp)),
                        dpt(m.distanceMeters).toFloat()
                    )
                }
        } else {
            emptyList()
        }
        with(dataBinding.chart) {
        }
    }
}