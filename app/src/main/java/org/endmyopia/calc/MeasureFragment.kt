package org.endmyopia.calc

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.rendering.Renderable
import kotlinx.android.synthetic.main.fragment_measure.*
import java.text.DecimalFormat

class MeasureFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure, container, false)
    }


    override fun onStart() {
        super.onStart()
        measure_text.text = "hello"
    }
}
