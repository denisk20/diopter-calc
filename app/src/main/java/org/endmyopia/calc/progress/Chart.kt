package org.endmyopia.calc.progress

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.endmyopia.calc.data.Measurement
import org.endmyopia.calc.data.MeasurementMode
import org.endmyopia.calc.util.debug
import org.endmyopia.calc.util.getColorRes
import org.endmyopia.calc.util.interpolate

/**
 * @author denisk
 * @since 26.06.2023.
 */
class Chart(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {
    private val circleRadiusDp = 5f
    private var circleRadiusPx = 0f
    private val lineWidthDp = 5f
    private var lineWidthPx = 0f

    private var minTimestamp = 0L
    private var maxTimestamp = 0L

    private var minDist = 0f
    private var maxDist = 0f

    private val paint = Paint()
    private val path = Path()

    private var allMeasurements = listOf<Measurement>()
    private var selectedModes: List<MeasurementMode> = listOf()

    private val allModesToXYs: MutableMap<MeasurementMode, FloatArray> = mutableMapOf()
    private var savedWidth = 0
    private var savedHeight = 0

    init {
        holder.addCallback(this)
        circleRadiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            circleRadiusDp,
            resources.displayMetrics
        )
        lineWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            lineWidthDp,
            resources.displayMetrics
        )

        paint.strokeWidth = lineWidthDp
        paint.style = Paint.Style.FILL_AND_STROKE
    }

    fun updateMeasurementCoords(measurements: List<Measurement>) {
        debug("updateMeasurementCoords ${measurements.size}")
        allMeasurements = measurements

        minTimestamp = Long.MAX_VALUE
        maxTimestamp = 0L
        minDist = Float.MAX_VALUE
        maxDist = 0f

        measurements.forEach {
            if (minTimestamp > it.date) minTimestamp = it.date
            if (maxTimestamp < it.date) maxTimestamp = it.date
            if (minDist > it.distanceMeters) minDist = it.distanceMeters.toFloat()
            if (maxDist < it.distanceMeters) maxDist = it.distanceMeters.toFloat()
        }
        val allModeToMeasurementsMap = measurements.groupBy { it.mode }
        allModesToXYs.clear()
        MeasurementMode.values().forEach { mode ->
            debug("getXYs for $mode")
            allModesToXYs.put(mode, getXYs(allModeToMeasurementsMap[mode].orEmpty()))
        }

        doRender()
    }

    fun updateModes(modes: List<MeasurementMode>) {
        debug("updateModes $modes")
        selectedModes = modes
        doRender()
    }

    private fun doRender() {
        if (width == 0 || height == 0 || allMeasurements.isEmpty()) return
        val canvas = holder.lockCanvas() ?: return
        canvas.drawColor(Color.WHITE)
        debug("width $savedWidth")
        selectedModes.forEach { mode ->
            val xyS = allModesToXYs[mode]
            debug("xys for mode $mode: ${xyS?.joinToString()}")
            if (xyS?.isNotEmpty() == true) {
                path.reset()
                path.moveTo(xyS[0], xyS[1])
                for (i in xyS.indices step 2) {
                    val x = xyS[i]
                    val y = xyS[i + 1]

                    if (i > 0) {
                        path.lineTo(x, y)
                    }
                    path.addCircle(x, y, circleRadiusDp, Path.Direction.CW)
                    path.moveTo(x, y)
                }
                paint.color = resources.getColor(mode.getColorRes(), null)
                canvas.drawPath(path, paint)
            }
        }
        holder.unlockCanvasAndPost(canvas)
        debug("rendered")
    }

    private fun getXYs(measurements: List<Measurement>): FloatArray {
        val xYs = FloatArray(measurements.size * 2)
        measurements.forEachIndexed { index, measurement ->
            val x = interpolate(minTimestamp, maxTimestamp, savedWidth, measurement.date)
            val y = interpolate(minDist, maxDist, savedHeight, measurement.distanceMeters.toFloat())

            debug("getting xy index $index date: ${measurement.date} date.toFloat: ${measurement.date.toFloat()} x: $x")

            val doubleIndex = index * 2
            xYs[doubleIndex] = x
            xYs[doubleIndex + 1] = savedHeight - y
        }
        return xYs
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, m: Int, width: Int, height: Int) {
        if (width > 0 && height > 0) {
            if (width != savedWidth || height != savedHeight) {
                savedWidth = width
                savedHeight = height
                updateMeasurementCoords(allMeasurements)
            }
        }
        doRender()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }
}