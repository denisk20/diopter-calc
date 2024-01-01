package org.endmyopia.calc.measure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
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
import org.endmyopia.calc.R
import org.endmyopia.calc.util.isEmulator
import kotlin.random.Random

class FaceArFragment : Fragment() {

    lateinit var sceneView: ARSceneView

    private var lastUpdate = -1L

    private var faceFound = false
    private var consequentEmptyFrames = 0
    private val CONSEQUENT_FRAMES_COUNT_LIMIT = 3

    private val UPDATE_INTERVAL = 500L //ms

    override fun onResume() {
        super.onResume()
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)
        if (holder.hasTakenMeasurement.value == true) {
            sceneView.session?.pause()
        } else
            sceneView.session?.resume()
    }

    /**
     * Override to turn off planeDiscoveryController. Plane trackables are not supported with the
     * front camera.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_face_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sceneView = view.findViewById<ARSceneView>(R.id.sceneView).apply {
            sessionConfiguration = { session, config ->
                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                val filter = CameraConfigFilter(session)
                filter.facingDirection = CameraConfig.FacingDirection.FRONT
                val supportedCameraConfigs = session.getSupportedCameraConfigs(filter)
                if (supportedCameraConfigs.size > 0) {
                    session.cameraConfig = supportedCameraConfigs[0]
                }
            }
            onSessionResumed =
                { it.setCameraTextureNames(sceneView.cameraStream?.cameraTextureIds) }
            onSessionUpdated = this@FaceArFragment::onSessionUpdated
        }
    }

    override fun onStart() {
        super.onStart()
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)

        holder.hasTakenMeasurement.observe(requireActivity(), androidx.lifecycle.Observer {
            handleTakenMeasurement(it)
        })
    }

    override fun onStop() {
        sceneView.onSessionUpdated = null
        super.onStop()
    }

    private fun handleTakenMeasurement(hasTakenIt: Boolean) {
        if (hasTakenIt) {
            sceneView.session?.pause()
            sceneView.visibility = GONE
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            consequentEmptyFrames = 0;
            sceneView.session?.resume()
            sceneView.visibility = VISIBLE
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
                (parentFragment as MeasureFragment).takeMeasurement()
            }
        }

        // Make new AugmentedFaceNodes for any new faces.
        for (face in faceList) {
            val nosePose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            val noseTranslation = nosePose.translation
            val distMeters =
                Math.sqrt((noseTranslation[0] * noseTranslation[0] + noseTranslation[1] * noseTranslation[1] + noseTranslation[2] * noseTranslation[2]).toDouble())
            (parentFragment as MeasureFragment).update(distMeters)
        }

        if (isEmulator()) {
            (parentFragment as MeasureFragment).update(Random.nextDouble(0.1, 1.5))
        }
        lastUpdate = now
    }
}
