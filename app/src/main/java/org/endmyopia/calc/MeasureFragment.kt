package org.endmyopia.calc

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_measure.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.databinding.FragmentMeasureBinding
import org.endmyopia.calc.db.AppDatabase
import org.endmyopia.calc.db.Measurement

class MeasureFragment : Fragment() {

    private lateinit var dataBinding: FragmentMeasureBinding
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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

        camera.setOnClickListener {
            if (dataBinding.holder?.hasTakenMeasurement?.value!!)
                reTakeMeasurement()
            else
                takeMeasurement()
        }
    }

    private fun ding() {
        if (!mediaPlayer.isPlaying) {
            //mediaPlayer.start()
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    fun update(distMeters: Double) {
        dataBinding.holder?.update(distMeters)
    }

    fun takeMeasurement() {
        dataBinding.holder?.hasTakenMeasurement?.postValue(true)
        activity?.let {
            GlobalScope.launch {
                val id = AppDatabase.getInstance(it.application).getMeasurementDao().insert(
                    Measurement(
                        0L,
                        System.currentTimeMillis(),
                        dataBinding.holder?.distanceMetersVal?.value ?: 0.0,
                        0.0
                    )
                )
                debug("measurementId: $id")
                dataBinding.holder?.lastPersistedMeasurementId = id
            }
            ding()
        }
    }

    private fun reTakeMeasurement() {
        dataBinding.holder?.hasTakenMeasurement?.postValue(false)
        activity?.let { activity ->
            dataBinding.holder?.let { holder ->
                GlobalScope.launch {
                    AppDatabase.getInstance(activity.application).getMeasurementDao().deleteById(
                        holder.lastPersistedMeasurementId
                    )
                }

            }
        }
    }
}
