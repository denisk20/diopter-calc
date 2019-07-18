package org.endmyopia.calc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author denisk
 * @since 2019-07-09.
 */
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val mode: MeasurementMode,
    val date: Long,
    val distanceMeters: Double,
    val calibrationCoeff: Double = 0.0
)
