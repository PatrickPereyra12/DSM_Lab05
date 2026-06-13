package com.example.amukisense.data.repository

import com.example.amukisense.data.local.dao.GpsGoogleDao
import com.example.amukisense.data.local.dao.GpsSensorsDao
import com.example.amukisense.data.local.entity.GpsGoogleEntity
import com.example.amukisense.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.flow.Flow

class GpsRepository(
    private val googleDao: com.example.amukisense.data.local.dao.GpsGoogleDao,
    private val sensorsDao: com.example.amukisense.data.local.dao.GpsSensorsDao
) {
    val googlePoints: Flow<List<com.example.amukisense.data.local.entity.GpsGoogleEntity>> = googleDao.observeAll()
    val sensorsPoints: Flow<List<com.example.amukisense.data.local.entity.GpsSensorsEntity>> = sensorsDao.observeAll()

    val googleCount: Flow<Int> = googleDao.observeCount()
    val sensorsCount: Flow<Int> = sensorsDao.observeCount()

    suspend fun saveGooglePoint(point: com.example.amukisense.data.local.entity.GpsGoogleEntity) = googleDao.insert(point)
    suspend fun saveSensorsPoint(point: com.example.amukisense.data.local.entity.GpsSensorsEntity) = sensorsDao.insert(point)

    suspend fun clearAll() {
        googleDao.deleteAll()
        sensorsDao.deleteAll()
    }
}