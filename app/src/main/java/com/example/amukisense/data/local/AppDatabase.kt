package com.example.amukisense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.amukisense.data.local.dao.AudioDao
import com.example.amukisense.data.local.dao.GpsGoogleDao
import com.example.amukisense.data.local.dao.GpsSensorsDao
import com.example.amukisense.data.local.dao.MediaDao
import com.example.amukisense.data.local.entity.AudioEntity
import com.example.amukisense.data.local.entity.GpsGoogleEntity
import com.example.amukisense.data.local.entity.GpsSensorsEntity
import com.example.amukisense.data.local.entity.MediaEntity


@Database(
    entities = [
        GpsGoogleEntity::class,
        GpsSensorsEntity::class,
        MediaEntity::class,
        AudioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsGoogleDao(): GpsGoogleDao
    abstract fun gpsSensorsDao(): GpsSensorsDao

    abstract fun mediaDao(): MediaDao

    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amukisense.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}