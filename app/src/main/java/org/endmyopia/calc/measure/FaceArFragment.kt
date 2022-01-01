package org.endmyopia.calc.measure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.ar.core.*
import com.google.ar.core.Config.AugmentedFaceMode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import org.endmyopia.calc.util.debug
import org.endmyopia.calc.util.isEmulator
import java.util.*
import kotlin.random.Random

class FaceArFragment : ArFragment() {


    private var lastUpdate = -1L

    private var faceFound = false
    private var consequentEmptyFrames = 0
    private val CONSEQUENT_FRAMES_COUNT_LIMIT = 3

    private val UPDATE_INTERVAL = 500L //ms

    val onUpdateListener: (FrameTime) -> Unit = {
        run {
            val now = System.currentTimeMillis()

            if (now - UPDATE_INTERVAL < lastUpdate) {
                return@run
            }
            val faceList = arSceneView.session!!.getAllTrackables(AugmentedFace::class.java)

            if (faceList.isNotEmpty()) {
                faceFound = true
            } else {
                consequentEmptyFrames++
                if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
                        "measure_with_gesture",
                        true
                    ) && faceFound && consequentEmptyFrames >= CONSEQUENT_FRAMES_COUNT_LIMIT
                ) {
                    faceFound = false;
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOnSessionConfigurationListener { session, config ->
            run {
                config.augmentedFaceMode = AugmentedFaceMode.MESH3D
                val filter = CameraConfigFilter(session)
                filter.facingDirection = CameraConfig.FacingDirection.FRONT
                val supportedCameraConfigs = session.getSupportedCameraConfigs(filter)
                if (supportedCameraConfigs != null && supportedCameraConfigs.size > 0) {
                    session.cameraConfig = supportedCameraConfigs[0]
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)
        if (holder.hasTakenMeasurement.value == true) {
            arSceneView.pause()
        } else
            arSceneView.resume()
        instructionsController.isEnabled = false
    }

    /**
     * Override to turn off planeDiscoveryController. Plane trackables are not supported with the
     * front camera.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val frameLayout =
            super.onCreateView(inflater, container, savedInstanceState) as FrameLayout?

        return frameLayout
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        //this prevents fullscreen, see https://github.com/google-ar/sceneform-android-sdk/issues/88
    }

    override fun onStart() {
        super.onStart()
        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        arSceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST)

        val holder: MeasureStateHolder =
            ViewModelProvider(requireActivity()).get(MeasureStateHolder::class.java)

        holder.hasTakenMeasurement.observe(requireActivity(), androidx.lifecycle.Observer {
            handleTakenMeasurement(it)
        })

        arSceneView.scene.addOnUpdateListener(onUpdateListener)
    }

    override fun onStop() {
        arSceneView.scene.removeOnUpdateListener(onUpdateListener)
        super.onStop()
    }

    private fun handleTakenMeasurement(hasTakenIt: Boolean) {
        debug("handleTakenMeasurement, $hasTakenIt")
        if (hasTakenIt) {
            arSceneView.pause()
            arSceneView.visibility = GONE
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            consequentEmptyFrames = 0;
            arSceneView.resume()
            arSceneView.visibility = VISIBLE
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}