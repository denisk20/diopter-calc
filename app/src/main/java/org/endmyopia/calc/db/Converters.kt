package org.endmyopia.calc.db

import androidx.room.TypeConverter
import org.endmyopia.calc.MeasurementMode

/**
 * @author denisk
 * @since 2019-07-17.
 */
class Converters {
    @TypeConverter
    fun measurementModeToInt(mode: MeasurementMode): Int {
        return mode.value
    }

    @TypeConverter
    fun intToMeasurementMode(value: Int): MeasurementMode {
        return MeasurementMode.byValue(value)
    }
}