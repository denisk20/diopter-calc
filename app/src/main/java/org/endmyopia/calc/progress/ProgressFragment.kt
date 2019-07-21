package org.endmyopia.calc.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.endmyopia.calc.R
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentProgressBinding


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
        })

        return view
    }

    private fun processFilterButtonChange(
        it: List<MeasurementMode>,
        button: Button,
        mode: MeasurementMode
    ) {
        button.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context!!,
                if (it.contains(mode)) R.color.gray else R.color.white
            )
        )
    }

    private fun addFilterOnClickListener(button: Button, mode: MeasurementMode) {
        button.setOnClickListener {
            val result: ArrayList<MeasurementMode> = ArrayList(
                dataBinding.holder?.selectedModes?.value
            )

            if (result.contains(mode) == true) {
                result.remove(mode)
            } else {
                result.add(mode)
            }
            dataBinding.holder?.selectedModes?.postValue(result)
        }
    }
}
