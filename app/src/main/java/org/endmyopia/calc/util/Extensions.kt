package org.endmyopia.calc.util

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.endmyopia.calc.R
import org.endmyopia.calc.data.MeasurementMode

/**
 * @author denisk
 * @since 2019-07-09.
 */

@StringRes
fun MeasurementMode.getLabelRes(): Int {
    return when (this) {
        MeasurementMode.LEFT -> R.string.left_eye
        MeasurementMode.RIGHT -> R.string.right_eye
        MeasurementMode.BOTH -> R.string.both_eyes
    }
}

@ColorRes
fun MeasurementMode.getColorRes(): Int {
    return when (this) {
        MeasurementMode.LEFT -> R.color.left
        MeasurementMode.RIGHT -> R.color.right
        MeasurementMode.BOTH -> R.color.both
    }
}
