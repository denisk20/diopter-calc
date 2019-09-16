package org.endmyopia.calc.progress

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding
import org.endmyopia.calc.util.getLabelRes


class ProgressFragment : Fragment() {

    private lateinit var dataBinding: FragmentProgressBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        dataBinding = FragmentProgressBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: ProgressStateHolder = ViewModelProviders.of(activity!!).get(ProgressStateHolder::class.java)
        dataBinding.holder = holder

        addFilterOnClickListener(dataBinding.filterLeft, MeasurementMode.LEFT)
        addFilterOnClickListener(dataBinding.filterBoth, MeasurementMode.BOTH)
        addFilterOnClickListener(dataBinding.filterRight, MeasurementMode.RIGHT)

        holder.selectedModes.observe(this, Observer {
            processFilterButtonChange(it, dataBinding.filterLeft, MeasurementMode.LEFT)
            processFilterButtonChange(it, dataBinding.filterBoth, MeasurementMode.BOTH)
            processFilterButtonChange(it, dataBinding.filterRight, MeasurementMode.RIGHT)

            GlobalScope.launch {
                val measurements =
                    AppDatabase.getInstance(context!!.applicationContext as Application).getMeasurementDao()
                        .getMeasurements(it)

                with(it) {
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

                dataBinding.chart.invalidate()
            }
        })

        return view
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
        val values = measurements.filter { m -> m.mode == mode }
            .map { m -> Entry(m.date.toFloat(), m.distanceMeters.toFloat()) }
        with(dataBinding) {
            if (chart.data == null) {
                chart.data = LineData()
            }
            if (chart.data.getDataSetByLabel(label, false) != null) {
                val dataSet = chart.data.getDataSetByLabel(label, false) as LineDataSet
                dataSet.values = values
                dataSet.notifyDataSetChanged()
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                val dataSet = LineDataSet(values, label)

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
            val result: ArrayList<MeasurementMode> = ArrayList(
                dataBinding.holder?.selectedModes?.value
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