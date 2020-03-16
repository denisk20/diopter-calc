package org.endmyopia.calc.util

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

/**
 * Can be used in combination with [when ] operator to force the branch checking at compile time.
 * It's useful in case you pass a sealed class or enum to the [when ] operator, so the compiler knows what else cases are missing.
 *
 * Example:
 *
 * sealed class Data {
 *   object One: Data()
 *   object Two: Data()
 * }
 *
 * when(data) {
 *   is One -> { }
 *   is Two -> { }
 * }.exhaustive
 */
val <T> T.exhaustive: T get() = this