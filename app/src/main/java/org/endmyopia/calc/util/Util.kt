package org.endmyopia.calc.util

import android.content.Context
import android.os.Build
import android.util.Log
import org.endmyopia.calc.R
import org.endmyopia.calc.data.MeasurementMode

/**
 * @author denisk
 * @since 2019-07-09.
 */
fun debug(text: String) = Log.e("=====", text)

fun isEmulator() = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || Build.PRODUCT.contains("sdk_google")
        || Build.PRODUCT.contains("google_sdk")
        || Build.PRODUCT.contains("sdk")
        || Build.PRODUCT.contains("sdk_x86")
        || Build.PRODUCT.contains("vbox86p")
        || Build.PRODUCT.contains("emulator")
        || Build.PRODUCT.contains("simulator")

fun getEyesText(mode: MeasurementMode, context: Context): String {
    return when (mode) {
        MeasurementMode.BOTH -> context.getString(R.string.both_eyes)
        MeasurementMode.LEFT -> context.getString(R.string.left_eye)
        MeasurementMode.RIGHT -> context.getString(R.string.right_eye)
    }
}
