package org.endmyopia.calc.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * @author denisk
 * @since 2019-07-06.
 */
@Database(
    version = 1,
    entities = [Measurement::class]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMeasurementDao(): MeasurementDao

    companion object {
        private lateinit var instance: AppDatabase

        fun getInstance(context: Application): AppDatabase {
            return if (!::instance.isInitialized)
                Room.databaseBuilder(context, AppDatabase::class.java, "diopter-calc.db").build()
                    .also { instance = it }
            else instance
        }
    }
}