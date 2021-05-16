package org.endmyopia.calc

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.ArCoreApk
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.help.HelpFragment
import org.endmyopia.calc.measure.MeasureFragment
import org.endmyopia.calc.progress.ProgressFragment
import org.endmyopia.calc.settings.SettingsFragment
import java.io.Reader
import java.lang.reflect.Type


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val volumePressedEvent = BroadcastChannel<Unit>(BUFFERED)

    fun <T> getListS(
        jsonArray: String?,
        clazz: Class<T>?
    ): List<T>? {
        val typeOfT: Type =
            TypeToken.getParameterized(MutableList::class.java, clazz).type
        return Gson().fromJson(jsonArray, typeOfT)
    }

    fun <T> getList(
        jsonArray: Reader?,
        clazz: Class<T>?
    ): List<T>? {
        val typeOfT: Type =
            TypeToken.getParameterized(MutableList::class.java, clazz).type
        return Gson().fromJson(jsonArray, typeOfT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        importIfRequested()

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val startMenuItem = nav_view.menu[savedInstanceState?.getInt(MENU_ITEM, 1) ?: 1]
        startMenuItem.setChecked(true)
        onNavigationItemSelected(startMenuItem)
    }

    private fun importIfRequested() {
        intent?.let { intent ->
            val uri = intent.data ?: intent.clipData?.getItemAt(0)?.uri
            if (uri != null) {
                contentResolver.openInputStream(uri)?.let { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        var measurements: List<Measurement>? = null
                        try {
                            measurements = getList(reader, Measurement::class.java)
                        } catch (e: JsonSyntaxException) {
                            Toast.makeText(
                                this,
                                getString(R.string.cant_import, e.message),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        measurements?.let { measurements ->
                            val dialogClickListener =
                                DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> {
                                            GlobalScope.launch {
                                                val measurementDao =
                                                    AppDatabase.getInstance(applicationContext as Application)
                                                        .getMeasurementDao()

                                                measurementDao.deleteAll()
                                                for (measurement in measurements) {
                                                    measurementDao.insert(measurement)
                                                }

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        R.string.import_complete,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                            dialog.dismiss()
                                        }
                                    }
                                }

                            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                            builder.setMessage(R.string.import_warn)
                                .setPositiveButton(R.string.yes, dialogClickListener)
                                .setNegativeButton(R.string.no, dialogClickListener).show()

                        }
                    }
                }
            }
        }
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
                    R.id.help -> {
                        HelpFragment()
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
        if (ArCoreApk.getInstance()
                .checkAvailability(activity) === ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE
        ) {
            Toast.makeText(activity, "Augmented Faces requires ArCore", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
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
