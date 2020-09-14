package org.endmyopia.calc.settings


import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.endmyopia.calc.R
import org.endmyopia.calc.data.AppDatabase
import org.endmyopia.calc.progress.ProgressStateHolder
import org.endmyopia.calc.util.debug
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    private val DIALOG_FRAGMENT_TAG = "NumberPickerDialog"
    private val pattern = "yyyy-MM-dd"
    private val simpleDateFormat = SimpleDateFormat(pattern)
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val button = findPreference<Preference>("export")
        button?.setOnPreferenceClickListener {
            debug("Clicked!")
            GlobalScope.launch {
                shareAllMeasurements()
            }
            true
        }
    }

    private fun shareAllMeasurements() {
        val measurements =
            AppDatabase.getInstance(requireContext().applicationContext as Application)
                .getMeasurementDao()
                .getMeasurements(ProgressStateHolder.initialModes)
        val json = gson.toJson(measurements)
        val outputDir: File = requireContext().cacheDir // context being the Activity pointer
        val date: String = simpleDateFormat.format(Date())
        val outputFile: File = File.createTempFile(date + '_', ".endmyopia.json", outputDir)

        outputFile.writeText(json)

        debug(json)

        val intent = Intent(Intent.ACTION_SEND)

        intent.type = "application/json"
        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".provider",
            outputFile
        );
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        val title = getString(R.string.sharing_progress, date)
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            title
        )
        intent.putExtra(Intent.EXTRA_TEXT, title)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(intent, title))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }
        if (preference is NumberPickerPreference) {
            val dialog = NumberPickerPreferenceDialog.newInstance(preference.key)
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}
