package org.endmyopia.calc.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * @author denisk
 * @since 2019-07-09.
 */
@Dao
interface MeasurementDao {
    @Insert
    fun insert(measurement: Measurement): Long

    @Query("DELETE FROM measurements WHERE id = :id")
    fun deleteById(id: Long)
}