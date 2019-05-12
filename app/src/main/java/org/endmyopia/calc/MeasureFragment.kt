package org.endmyopia.calc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_measure.*
import org.endmyopia.calc.databinding.FragmentMeasureBinding

class MeasureFragment : Fragment() {

    lateinit var dataBinding: FragmentMeasureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        dataBinding = FragmentMeasureBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: MeasureStateHolder = ViewModelProviders.of(activity!!).get(MeasureStateHolder::class.java)
        dataBinding.holder = holder

        return view
    }

    override fun onStart() {
        super.onStart()
        camera.setOnClickListener { view ->
            if (dataBinding.holder?.hasTakenMeasurement?.value!!) {
                //arView.arSceneView.resume()
                dataBinding.holder?.hasTakenMeasurement?.postValue(false)
            } else {
                //arView.arSceneView.pause()
                dataBinding.holder?.hasTakenMeasurement?.postValue(true)
                Snackbar.make(view, R.string.measurement_taken, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
    }

    fun update(dist: Double, diopts: Double) {
        dataBinding.holder?.update(dist, diopts)
    }
}
