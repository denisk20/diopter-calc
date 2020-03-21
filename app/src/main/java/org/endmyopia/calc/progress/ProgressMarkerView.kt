package org.endmyopia.calc.progress

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import org.endmyopia.calc.R
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.measure.MeasureStateHolder
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author denisk
 * @since 2019-11-24.
 */
class ProgressMarkerView(context: Context, layoutResource: Int) : MarkerView(
    context,
    layoutResource
) {
    val DATE_FORMAT = SimpleDateFormat("MMM-dd-yyyy")
    val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")

    private val date: TextView = findViewById(R.id.date)
    private val time: TextView = findViewById(R.id.time)
    private val eyes: TextView = findViewById(R.id.eyes)
    private val value: TextView = findViewById(R.id.value)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let { entry ->
            entry.data?.let {
                if (it is Measurement) {
                    val dateVal = Date(it.date)
                    date.text = DATE_FORMAT.format(dateVal)
                    time.text = TIME_FORMAT.format(dateVal)
                    eyes.text = when (it.mode) {
                        MeasurementMode.BOTH -> context.getString(R.string.both_eyes)
                        MeasurementMode.LEFT -> context.getString(R.string.left_eye)
                        MeasurementMode.RIGHT -> context.getString(R.string.right_eye)
                    }
                    value.text = MeasureStateHolder.formatDiopt.format(it.distanceMeters)

                }
            }
        }
        super.refreshContent(e, highlight)
    }
}