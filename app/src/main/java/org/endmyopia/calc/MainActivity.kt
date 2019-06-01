package org.endmyopia.calc

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.ArCoreApk
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var arCoreInstallRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val item = nav_view.menu.get(0)
        item.setChecked(true)
        onNavigationItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, getString(R.string.camera_permission), Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        when (item.itemId) {
            R.id.measure -> {
                fragmentTransaction.replace(R.id.content, MeasureFragment())
            }
            R.id.progress -> {
                fragmentTransaction.replace(R.id.content, ProgressFragment())
            }
            R.id.settings -> {
                fragmentTransaction.replace(R.id.content, SettingsFragment())
            }
        }

        fragmentTransaction.commit()
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     *
     * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     *
     * Finishes the activity if Sceneform can not run
     */
    fun checkIsSupportedDeviceOrFinish(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({ checkIsSupportedDeviceOrFinish() }, 200)

        }

        if (availability in listOf(
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED,
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD
            )
        ) {
            ArCoreApk.getInstance().requestInstall(this, true)
        }
        if (ArCoreApk.getInstance().checkAvailability(this) === ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "Diopter Calc requires ArCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < 3.0) {
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            finish()
            return false
        }
        return true
    }

}
