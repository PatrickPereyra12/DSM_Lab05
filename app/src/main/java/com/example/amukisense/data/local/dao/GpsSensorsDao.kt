package com.example.amukisense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.amukisense.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsSensorsDao {
    @Insert
    suspend fun insert(item: com.example.amukisense.data.local.entity.GpsSensorsEntity): Long

    @Query("SELECT * FROM gps_sensors ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<com.example.amukisense.data.local.entity.GpsSensorsEntity>>

    @Query("SELECT COUNT(*) FROM gps_sensors")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM gps_sensors")
    suspend fun deleteAll()
}