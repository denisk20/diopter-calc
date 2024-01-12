package org.endmyopia.calc.measure

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.ar.core.AugmentedFace
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.databinding.FragmentMeasureBinding
import org.endmyopia.calc.util.debug
import org.endmyopia.calc.util.isEmulator
import kotlin.random.Random

class MeasureFragment : Fragment() {

    lateinit var sceneView: ARSceneView

    private var lastUpdate = -1L

    private var faceFound = false
    private var consequentEmptyFrames = 0
    private val CONSEQUENT_FRAMES_COUNT_LIMIT = 3

    private val UPDATE_INTERVAL = 500L //ms

    private lateinit var dataBinding: FragmentMeasureBinding
    private lateinit var mediaPlayer: MediaPlayer

    val COVER_NOSE_TIP_SHOWN = "COVER_NOSE_TIP_SHOWN"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        dataBinding = FragmentMeasureBinding.bind(view)
        dataBinding.lifecycleOwner = this
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)
        dataBinding.holder = holder

        sceneView = dataBinding.sceneView.apply {
            sessionConfiguration = { session, config ->
                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                val filter = CameraConfigFilter(session)
                filter.facingDirection = CameraConfig.FacingDirection.FRONT
                val supportedCameraConfigs = session.getSupportedCameraConfigs(filter)
                if (supportedCameraConfigs.size > 0) {
                    session.cameraConfig = supportedCameraConfigs[0]
                }
            }
            onSessionResumed = {
                it.setCameraTextureNames(sceneView.cameraStream?.cameraTextureIds)
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)
        val hasTakenMeasurement = holder.hasTakenMeasurement.value == true
        updateArSession(hasTakenMeasurement)

    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(context, R.raw.dingaling)

        dataBinding.camera.setOnClickListener {
            if (dataBinding.holder?.hasTakenMeasurement?.value!!)
                dataBinding.holder?.hasTakenMeasurement?.postValue(false)
            else
                takeMeasurement()
        }

        dataBinding.delete.setOnClickListener {
            deleteMeasurement()
        }

        dataBinding.leftEye.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.LEFT,
                R.string.left_eye
            )
        )
        dataBinding.rightEye.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.RIGHT,
                R.string.right_eye
            )
        )
        dataBinding.bothEyes.setOnClickListener(
            getEyeModeChangeFn(
                MeasurementMode.BOTH,
                R.string.both_eyes
            )
        )
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)

        holder.hasTakenMeasurement.observe(requireActivity(), androidx.lifecycle.Observer {
            updateArSession(it)
        })
    }

    private fun updateArSession(hasTakenIt: Boolean) {
        if (hasTakenIt) {
            sceneView.session?.pause()
            sceneView.onSessionUpdated = null
            sceneView.visibility = View.GONE
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            consequentEmptyFrames = 0
            sceneView.onSessionUpdated = this@MeasureFragment::onSessionUpdated
            sceneView.session?.resume()
            sceneView.visibility = View.VISIBLE
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun onSessionUpdated(session: Session, frame: Frame) {
        val now = System.currentTimeMillis()
        if (now - UPDATE_INTERVAL < lastUpdate) {
            return
        }
        val faceList = session.getAllTrackables(AugmentedFace::class.java)

        if (faceList.isNotEmpty()) {
            faceFound = true
        } else {
            consequentEmptyFrames++
            if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                    "measure_with_gesture",
                    true
                ) && faceFound && consequentEmptyFrames >= CONSEQUENT_FRAMES_COUNT_LIMIT
            ) {
                faceFound = false
                consequentEmptyFrames = 0
                takeMeasurement()
            }
        }

        // Make new AugmentedFaceNodes for any new faces.
        for (face in faceList) {
            val nosePose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            val noseTranslation = nosePose.translation
            val distMeters =
                Math.sqrt((noseTranslation[0] * noseTranslation[0] + noseTranslation[1] * noseTranslation[1] + noseTranslation[2] * noseTranslation[2]).toDouble())
            update(distMeters)
        }

        if (isEmulator()) {
            update(Random.nextDouble(0.1, 1.5))
        }
        lastUpdate = now
    }

    private fun getEyeModeChangeFn(mode: MeasurementMode, @StringRes resId: Int): (View) -> Unit {
        return {
            dataBinding.holder?.mode?.postValue(mode)
            Toast.makeText(
                context,
                resources.getString(R.string.measuring, resources.getString(resId)),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ding() {
        if (!mediaPlayer.isPlaying && PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("play_sound", true)
        ) {
            mediaPlayer.start()
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
        if (dataBinding.holder?.hasTakenMeasurement?.value == true) {
            return
        }
        dataBinding.holder?.hasTakenMeasurement?.postValue(true)
        activity?.let {
            GlobalScope.launch {
                val id = AppDatabase.getInstance(it.application).getMeasurementDao().insert(
                    Measurement(
                        0L,
                        dataBinding.holder?.mode?.value ?: MeasurementMode.BOTH,
                        System.currentTimeMillis(),
                        dataBinding.holder?.distanceMetersVal?.value ?: 0.0,
                        0.0
                    )
                )
                debug("measurementId: $id")
                dataBinding.holder?.lastPersistedMeasurementId = id
            }
            ding()
            showProTip()

        }
    }

    private fun showProTip() {
        if (!PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean(COVER_NOSE_TIP_SHOWN, false)
        ) {
            Toast.makeText(context, R.string.cover_nose_tip, Toast.LENGTH_LONG).show()
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                .putBoolean(COVER_NOSE_TIP_SHOWN, true).apply()
        }
    }

    private fun deleteMeasurement() {
        Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
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

enum class FocusStyle {
    White, Black, Color
}
