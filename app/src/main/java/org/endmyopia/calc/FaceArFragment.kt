package org.endmyopia.calc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProviders
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Config
import com.google.ar.core.Config.AugmentedFaceMode
import com.google.ar.core.Session
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import java.util.*

/** Implements ArFragment and configures the session for using the augmented faces feature.  */
class FaceArFragment : ArFragment() {

    private var lastUpdate = -1L

    private var faceFound = false
    private var consequentEmptyFrames = 0
    private val CONSEQUENT_FRAMES_COUNT_LIMIT = 3

    private val UPDATE_INTERVAL = 500L //ms

    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        config.augmentedFaceMode = AugmentedFaceMode.MESH3D
        return config
    }

    override fun getSessionFeatures(): Set<Session.Feature> {
        return EnumSet.of<Session.Feature>(Session.Feature.FRONT_CAMERA)
    }

    /**
     * Override to turn off planeDiscoveryController. Plane trackables are not supported with the
     * front camera.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val frameLayout = super.onCreateView(inflater, container, savedInstanceState) as FrameLayout?

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)

        return frameLayout
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        //this prevents fullscreen, see https://github.com/google-ar/sceneform-android-sdk/issues/88
    }

    override fun onStart() {
        super.onStart()

        // This is important to make sure that the camera stream renders first so that
        // the face mesh occlusion works correctly.
        arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val holder: MeasureStateHolder = ViewModelProviders.of(activity!!).get(MeasureStateHolder::class.java)

        holder.hasTakenMeasurement.observe(activity!!, androidx.lifecycle.Observer {
            handeTakenMeasurement(it)
        })


        arSceneView.scene.addOnUpdateListener {
            run {
                val now = System.currentTimeMillis()

                if(now - UPDATE_INTERVAL < lastUpdate) {
                    return@run
                }
                val faceList = arSceneView.session!!.getAllTrackables(AugmentedFace::class.java)

                if (faceList.size > 0) {
                    faceFound = true
                } else {
                    consequentEmptyFrames++
                    if (faceFound && consequentEmptyFrames >= CONSEQUENT_FRAMES_COUNT_LIMIT) {
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

                lastUpdate = now
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val holder: MeasureStateHolder = ViewModelProviders.of(activity!!).get(MeasureStateHolder::class.java)
        val hasTakenIt = holder.hasTakenMeasurement.value!!
        handeTakenMeasurement(hasTakenIt)
    }

    private fun handeTakenMeasurement(hasTakenIt: Boolean) {
        if (hasTakenIt) {
            arSceneView.pause()
            arSceneView.visibility = GONE
        } else {
            consequentEmptyFrames = 0;
            arSceneView.resume()
            arSceneView.visibility = VISIBLE
        }
    }
}