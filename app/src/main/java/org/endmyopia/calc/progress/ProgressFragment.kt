package org.endmyopia.calc.progress

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding
import org.endmyopia.calc.measure.MeasureStateHolder
import org.endmyopia.calc.util.dpt
import org.endmyopia.calc.util.getEyesText
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProgressFragment : Fragment() {

    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)

    private lateinit var dataBinding: FragmentProgressBinding
    private lateinit var deleteDialogBuilder: AlertDialog.Builder


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        dataBinding = FragmentProgressBinding.bind(view)
        dataBinding.lifecycleOwner = this
        dataBinding.progressPager.adapter = PagerAdapter(this)
        TabLayoutMediator(dataBinding.dots, dataBinding.progressPager) { tab, position ->
            dataBinding.dots.selectTab(tab, true)
        }.attach()
        with(dataBinding) {

            val holder: ProgressStateHolder =
                ViewModelProvider(requireActivity()).get(ProgressStateHolder::class.java)
            this.holder = holder
            //chart.axisLeft.axisMinimum = yAxisShift
            //chart.axisRight.axisMinimum = yAxisShift

            deleteDialogBuilder = AlertDialog.Builder(requireContext())

            fillData()

            addFilterOnClickListener(filterLeft, MeasurementMode.LEFT)
            addFilterOnClickListener(filterBoth, MeasurementMode.BOTH)
            addFilterOnClickListener(filterRight, MeasurementMode.RIGHT)

            delete.setOnClickListener {
                holder.selectedValue.value?.let {
                    deleteDialogBuilder
                        .setTitle(
                            getString(
                                R.string.delete_measurement,
                                MeasureStateHolder.formatDiopt.format(dpt(it.distanceMeters)),
                                getEyesText(it.mode, requireContext())
                            )
                        )
                        .setPositiveButton(
                            R.string.yes
                        ) { _, i ->
                            holder.selectedValue.value?.let { measurement ->
                                GlobalScope.launch {
                                    holder.selectedValue.postValue(null)
                                    AppDatabase.getInstance(requireContext().applicationContext as Application)
                                        .getMeasurementDao().deleteById(measurement.id)
                                    fillData()
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

    private fun fillData() {
        GlobalScope.launch {
            dataBinding.holder?.let {
                it.selectedValue.postValue(null)
                val measurements =
                    AppDatabase.getInstance(requireContext().applicationContext as Application)
                        .getMeasurementDao()
                        .getMeasurements(ProgressStateHolder.initialModes)
//                val measurements = listOf(
//                    Measurement(1, MeasurementMode.BOTH, 1582520777860, 0.3),
//                    Measurement(2, MeasurementMode.BOTH, 1582520780860, 0.79),
//                    Measurement(3, MeasurementMode.BOTH, 1582520785860, 0.20),
//                    Measurement(4, MeasurementMode.BOTH, 1582520797860, 0.40),
//                    Measurement(5, MeasurementMode.BOTH, 1582520975860, 0.50),
//
//                    Measurement(1, MeasurementMode.LEFT, 1582520776860, 0.5),
//                    Measurement(2, MeasurementMode.LEFT, 1582520777860, 0.61),
//                    Measurement(3, MeasurementMode.LEFT, 1582520779860, 0.63),
//                    Measurement(4, MeasurementMode.LEFT, 1582520796860, 0.60),
//                    Measurement(5, MeasurementMode.LEFT, 1582520974860, 0.65),
//                    Measurement(5, MeasurementMode.LEFT, 1582520977860, 0.82),
//
//                    Measurement(1, MeasurementMode.RIGHT, 1582520777860, 0.9),
//                    Measurement(2, MeasurementMode.RIGHT, 1582520782860, 0.2),
//                    Measurement(3, MeasurementMode.RIGHT, 1582520787860, 0.4),
//                    Measurement(4, MeasurementMode.RIGHT, 1582520799860, 0.3),
//                    Measurement(5, MeasurementMode.RIGHT, 1582520984860, 0.7)
//                )

                it.data.postValue(measurements)
            }
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
