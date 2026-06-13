package com.example.amukisense

import android.app.Application
import com.example.amukisense.data.local.AppDatabase
import com.example.amukisense.data.local.FileStorageManager
import com.example.amukisense.data.repository.AudioRepository
import com.example.amukisense.data.repository.GpsRepository
import com.example.amukisense.data.repository.MediaRepository
import com.example.amukisense.data.session.SessionManager

class AmukiSenseApp : Application() {

    // Inicialización perezosa (lazy) para no saturar la memoria al abrir la app
    val database     by lazy { AppDatabase.getDatabase(this) }
    val fileStorage  by lazy { FileStorageManager(this) }
    val sessionManager by lazy { SessionManager(this) }

    val gpsRepository by lazy {
        GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
    }
    val mediaRepository by lazy {
        MediaRepository(database.mediaDao(), fileStorage)
    }
    val audioRepository by lazy {
        AudioRepository(database.audioDao(), fileStorage)
    }
}