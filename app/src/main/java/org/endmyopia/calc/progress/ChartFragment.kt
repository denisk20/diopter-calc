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
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @author denisk
 * @since 01.01.2022.
 */
class ChartFragment : Fragment() {

    val groupLimit = 150
    val totalLimit = groupLimit * 3

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

        holder.selectedModes.observe(viewLifecycleOwner) { modes ->
            dataBinding.chart.updateModes(modes)
        }

        holder.data.observe(viewLifecycleOwner) { measurements ->
            if (measurements.size > totalLimit) {
                val map = measurements?.groupBy { it.mode }.orEmpty()
                val avgsGlobalMap = mutableMapOf<MeasurementMode, List<Measurement>>()
                for (entry in map.entries) {
                    val measurementsForMode = map.getOrDefault(entry.key, listOf())
                    var meas = measurementsForMode
                    if (measurementsForMode.size > groupLimit) {
                        val groupSize =
                            ceil(measurementsForMode.size / groupLimit.toFloat()).toInt()
                        val groupsCount =
                            floor(measurementsForMode.size / groupSize.toFloat()).toInt()
                        val avgMeasurements = mutableListOf<Measurement>()
                        for (i in measurementsForMode.indices step groupSize) {
                            if (i + groupSize > measurementsForMode.size) {
                                continue
                            }
                            val subList = measurementsForMode.subList(i, i + groupSize)
                            val avgDate = subList.map { it.date }.average().toLong()
                            val avgDistance = subList.map { it.distanceMeters }.average()
                            val measurement = Measurement(0, entry.key, avgDate, avgDistance, 0.0)
                            avgMeasurements.add(measurement)
                        }
                        if (groupsCount < groupLimit) {
                            avgMeasurements.addAll(measurementsForMode.takeLast(groupLimit - groupsCount))
                        }
                        meas = avgMeasurements
                    }
                    avgsGlobalMap.put(entry.key, meas)
                }
                dataBinding.chart.updateMeasurementCoords(avgsGlobalMap.flatMap { it.value })
            } else {
                dataBinding.chart.updateMeasurementCoords(measurements)
            }

        }

        return view
    }
}