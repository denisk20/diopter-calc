package org.endmyopia.calc.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.endmyopia.calc.R
import org.endmyopia.calc.databinding.FragmentChartBinding

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

        holder.selectedModes.observe(viewLifecycleOwner) { modes ->
            dataBinding.chart.updateModes(modes)
        }

        holder.data.observe(viewLifecycleOwner) { measurements ->
            dataBinding.chart.updateMeasurementCoords(measurements)
        }

        return view
    }
}