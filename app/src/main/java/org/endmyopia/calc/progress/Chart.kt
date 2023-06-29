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
import org.endmyopia.calc.measure.MeasureStateHolder
import org.endmyopia.calc.util.dpt
import org.endmyopia.calc.util.getColorRes
import org.endmyopia.calc.util.interpolate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * @author denisk
 * @since 26.06.2023.
 */
class Chart(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {
    private val circleRadiusDp = 8f
    private var circleRadiusPx = 0f
    private val lineWidthDp = 5f
    private var lineWidthPx = 0f
    private val marginDp = 30f
    private var marginPx = 0
    private var offset = 0f

    private val labelOffsetDp = 5f
    private var labelOffsetPx = 0f

    private val textSizeSp = 16f

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

    private var minDioptLabel = ""
    private var maxDioptLabel = ""
    private var minTimestampLabel = ""
    private var maxTimestampLabel = ""

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
        val marg = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            marginDp,
            resources.displayMetrics
        )

        labelOffsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            labelOffsetDp,
            resources.displayMetrics
        )

        marginPx = marg.roundToInt()
        offset = marg / 2

        paint.strokeWidth = lineWidthDp
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            textSizeSp,
            resources.displayMetrics
        )
    }

    fun updateMeasurementCoords(measurements: List<Measurement>) {
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

        maxDioptLabel = MeasureStateHolder.formatDiopt.format(dpt(maxDist.toDouble()))
        minDioptLabel = MeasureStateHolder.formatDiopt.format(dpt(minDist.toDouble()))
        minTimestampLabel = SimpleDateFormat.getDateInstance().format(Date(minTimestamp))
        maxTimestampLabel = SimpleDateFormat.getDateInstance().format(Date(maxTimestamp))

        val allModeToMeasurementsMap = measurements.groupBy { it.mode }
        allModesToXYs.clear()
        MeasurementMode.values().forEach { mode ->
            allModesToXYs.put(mode, getXYs(allModeToMeasurementsMap[mode].orEmpty()))
        }

        doRender()
    }

    fun updateModes(modes: List<MeasurementMode>) {
        selectedModes = modes
        doRender()
    }

    private fun doRender() {
        if (width == 0 || height == 0 || allMeasurements.isEmpty()) return
        val canvas = holder.lockCanvas() ?: return
        canvas.drawColor(Color.WHITE)
        selectedModes.forEach { mode ->
            val xyS = allModesToXYs[mode]
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

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        canvas.drawText(
            maxDioptLabel,
            savedWidth - marginPx * 2f,
            marginPx * 1f,
            paint
        )
        canvas.drawText(
            minDioptLabel,
            savedWidth - marginPx * 2f,
            savedHeight.toFloat() + marginPx,
            paint
        )

        canvas.drawText(
            minTimestampLabel,
            labelOffsetPx,
            savedHeight.toFloat() + marginPx * 2 - labelOffsetPx,
            paint
        )
        canvas.drawText(
            maxTimestampLabel,
            savedWidth - marginPx * 2f - labelOffsetPx,
            savedHeight.toFloat() + marginPx * 2 - labelOffsetPx,
            paint
        )
        paint.style = Paint.Style.FILL_AND_STROKE

        holder.unlockCanvasAndPost(canvas)
    }

    private fun getXYs(measurements: List<Measurement>): FloatArray {
        val xYs = FloatArray(measurements.size * 2)
        measurements.forEachIndexed { index, measurement ->
            val x = interpolate(minTimestamp, maxTimestamp, savedWidth, measurement.date, offset)
            val y = interpolate(
                minDist,
                maxDist,
                savedHeight,
                measurement.distanceMeters.toFloat(),
                -offset
            )

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
                savedWidth = width - marginPx
                savedHeight = height - marginPx * 2
                updateMeasurementCoords(allMeasurements)
            }
        }
        doRender()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }
}