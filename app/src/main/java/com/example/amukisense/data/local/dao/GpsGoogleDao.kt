package com.example.amukisense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.amukisense.data.local.entity.GpsGoogleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsGoogleDao {
    @Insert
    suspend fun insert(item: com.example.amukisense.data.local.entity.GpsGoogleEntity): Long

    @Query("SELECT * FROM gps_google ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<com.example.amukisense.data.local.entity.GpsGoogleEntity>>

    @Query("SELECT COUNT(*) FROM gps_google")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM gps_google")
    suspend fun deleteAll()
}