package org.endmyopia.calc.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.endmyopia.calc.R
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProgressFragment : Fragment() {

    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)

    private lateinit var dataBinding: FragmentProgressBinding

    private lateinit var holder: ProgressStateHolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        dataBinding = FragmentProgressBinding.bind(view)
        dataBinding.lifecycleOwner = this
        dataBinding.progressPager.adapter = PagerAdapter(this)
        holder = ViewModelProvider(requireActivity()).get(ProgressStateHolder::class.java)
        TabLayoutMediator(dataBinding.dots, dataBinding.progressPager) { tab, position ->
            dataBinding.dots.selectTab(tab, true)
            holder.selectedValue.postValue(null)
        }.attach()
        with(dataBinding) {

            //chart.axisLeft.axisMinimum = yAxisShift
            //chart.axisRight.axisMinimum = yAxisShift

            holder.fillData(requireContext())

            addFilterOnClickListener(filterLeft, MeasurementMode.LEFT)
            addFilterOnClickListener(filterBoth, MeasurementMode.BOTH)
            addFilterOnClickListener(filterRight, MeasurementMode.RIGHT)
        }

        return view
    }

    private fun addFilterOnClickListener(button: ToggleButton, mode: MeasurementMode) {
        button.setOnCheckedChangeListener { _, checked ->
            val result: ArrayList<MeasurementMode> = ArrayList(
                holder.selectedModes.value ?: ArrayList<MeasurementMode>()
            )

            if (checked) {
                result.add(mode)
            } else {
                result.remove(mode)
            }
            holder.selectedModes.postValue(result)
        }
    }

    private inner class PagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            if (position == 0) {
                return ChartFragment()
            }
            return TableFragment()
        }
    }
}
