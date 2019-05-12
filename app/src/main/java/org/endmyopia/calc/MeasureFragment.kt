package org.endmyopia.calc

import android.media.MediaPlayer
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
    lateinit var mediaPlayer: MediaPlayer

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
        mediaPlayer = MediaPlayer.create(context, R.raw.dingaling)

        camera.setOnClickListener { view ->
            if (dataBinding.holder?.hasTakenMeasurement?.value!!) {
                dataBinding.holder?.hasTakenMeasurement?.postValue(false)
            } else {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                dataBinding.holder?.hasTakenMeasurement?.postValue(true)
                Snackbar.make(view, R.string.measurement_taken, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun update(dist: Double, diopts: Double) {
        dataBinding.holder?.update(dist, diopts)
    }
}
