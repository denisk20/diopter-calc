package org.endmyopia.calc

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.ArCoreApk
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import org.endmyopia.calc.measure.MeasureFragment
import org.endmyopia.calc.progress.ProgressFragment
import org.endmyopia.calc.settings.SettingsFragment
import org.endmyopia.calc.util.debug

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val volumePressedEvent = BroadcastChannel<Unit>(BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
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

        val startMenuItem = nav_view.menu[savedInstanceState?.getInt(MENU_ITEM, 0) ?: 0]
        startMenuItem.setChecked(true)
        onNavigationItemSelected(startMenuItem)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var index = 0
        nav_view.menu.forEachIndexed { theIndex, theItem ->
            if (theItem.isChecked) {
                index = theIndex
            }
        }

        outState.putInt(MENU_ITEM, index)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val count = supportFragmentManager.backStackEntryCount
            if (count == 1)
                finish()
            else {
                super.onBackPressed()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, objEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
            return true
        }
        return super.onKeyUp(keyCode, objEvent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val tag = item.itemId.toString()
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentTransaction.replace(
                R.id.content,
                fragment,
                tag
            )
        } else {
            debug("Fragment created")
            fragmentTransaction.replace(
                R.id.content,
                when (item.itemId) {
                    R.id.measure -> {
                        MeasureFragment()
                    }
                    R.id.progress -> {
                        ProgressFragment()
                    }
                    R.id.settings -> {
                        SettingsFragment()
                    }
                    else -> throw IllegalArgumentException("Unknown menu item ${item.itemId}")
                },
                tag
            )
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            volumePressedEvent.offer(Unit)
        }
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
    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (ArCoreApk.getInstance().checkAvailability(activity) === ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(activity, "Augmented Faces requires ArCore", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < 3.0) {
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    companion object {
        const val MENU_ITEM = "selected_menu_item"
    }
}
