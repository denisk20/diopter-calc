package org.endmyopia.calc

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.rendering.Renderable
import kotlinx.android.synthetic.main.fragment_measure.*
import org.endmyopia.calc.databinding.FragmentMeasureBinding
import java.text.DecimalFormat

class MeasureFragment : Fragment() {

    lateinit var dataBinding: FragmentMeasureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        dataBinding = FragmentMeasureBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: MeasureStateHolder = ViewModelProviders.of(this).get(MeasureStateHolder::class.java)
        dataBinding.holder = holder

        return view
    }

    override fun onStart() {
        super.onStart()
        camera.setOnClickListener { view ->
            Snackbar.make(view, R.string.measurement_taken, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()


        }
    }

    fun update(dist: Double, diopts: Double) {
        dataBinding.holder?.update(dist, diopts)
    }
}
