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

        deleteDialogBuilder = AlertDialog.Builder(context!!)

        addFilterOnClickListener(dataBinding.filterLeft, MeasurementMode.LEFT)
        addFilterOnClickListener(dataBinding.filterBoth, MeasurementMode.BOTH)
        addFilterOnClickListener(dataBinding.filterRight, MeasurementMode.RIGHT)

        holder.selectedModes.observe(this, Observer {
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
                            AppDatabase.getInstance(context!!.applicationContext as Application)
                                .getMeasurementDao().deleteById(measurement.id)
                            val dataSetByLabel = dataBinding.chart.data.getDataSetByLabel(
                                getString(measurement.mode.getLabelRes()),
                                false
                            )
                            if (dataSetByLabel is LineDataSet) {
                                debug("removed? -- ${dataSetByLabel.removeEntryByXValue(measurement.date.toFloat())}")
                                dataSetByLabel.notifyDataSetChanged()
                                dataBinding.chart.data.notifyDataChanged()
                                dataBinding.chart.notifyDataSetChanged()
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

            dataBinding.chart.invalidate()
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
        val values = measurements.filter { m -> m.mode == mode }
            .map { m -> Entry(m.date.toFloat(), m.distanceMeters.toFloat(), m) }
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
                chart.axisLeft.axisMinimum = yAxisShift
                chart.axisRight.axisMinimum = yAxisShift
                chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onNothingSelected() {
                        holder?.selectedValue?.postValue(null)
                    }

                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        holder?.selectedValue?.postValue(e?.data as Measurement?)
                    }
                })
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
