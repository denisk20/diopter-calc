package org.endmyopia.calc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Config
import com.google.ar.core.Config.AugmentedFaceMode
import com.google.ar.core.Session
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import java.text.DecimalFormat
import java.util.EnumSet

/** Implements ArFragment and configures the session for using the augmented faces feature.  */
class FaceArFragment : ArFragment() {

    private val format = DecimalFormat(".00")

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

        val scene = arSceneView.scene

        scene.addOnUpdateListener { frameTime ->
            run {
                val faceList = arSceneView.session!!.getAllTrackables(AugmentedFace::class.java)

                // Make new AugmentedFaceNodes for any new faces.
                for (face in faceList) {
                    val translation = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP).translation
                    Log.e("=======", format.format(Math.sqrt((translation[0] * translation[0] + translation[1] * translation[1] + translation[2] * translation[2]).toDouble())))
                }
            }
        }
    }
}