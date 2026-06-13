package com.example.amukisense.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.amukisense.data.local.AppDatabase
import com.example.amukisense.data.local.entity.GpsGoogleEntity
import com.example.amukisense.data.local.entity.GpsSensorsEntity
import com.example.amukisense.data.repository.GpsRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class GpsCaptureService : Service() {

    companion object {
        private const val INTERVAL_MS = 10_000L
        private const val SENSOR_TIMEOUT_MS = 5_000L
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "gps_capture_channel"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null

    // Inicialización manual del repositorio conectando la BD local
    private val gpsRepo by lazy {
        val db = AppDatabase.getDatabase(applicationContext)
        _root_ide_package_.com.example.amukisense.data.repository.GpsRepository(
            db.gpsGoogleDao(),
            db.gpsSensorsDao()
        )
    }

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (captureJob == null) {
            captureJob = scope.launch {
                while (isActive) {
                    performCaptures()
                    delay(INTERVAL_MS)
                }
            }
        }
        return START_STICKY
    }

    private suspend fun performCaptures() {
        val now = System.currentTimeMillis()

        // 1. Captura desde Google FLP
        try {
            val loc = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await() // Suspende la corutina sin bloquear el hilo gracias a play-services-location

            loc?.let {
                gpsRepo.saveGooglePoint(
                    _root_ide_package_.com.example.amukisense.data.local.entity.GpsGoogleEntity(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        speed = if (it.hasSpeed()) it.speed else null,
                        bearing = if (it.hasBearing()) it.bearing else null,
                        timestamp = now
                    )
                )
            }
        } catch (e: Exception) {
            // Manejo de excepciones en caso de revocación de permisos o fallas del proveedor
        }

        // 2. Captura desde el Sensor de Hardware de manera pura (con Timeout de 5s)
        try {
            val sensorLoc = withTimeoutOrNull(SENSOR_TIMEOUT_MS) {
                getRawGpsLocation()
            }
            gpsRepo.saveSensorsPoint(
                _root_ide_package_.com.example.amukisense.data.local.entity.GpsSensorsEntity(
                    latitude = sensorLoc?.latitude, // null si se cumple el timeout
                    longitude = sensorLoc?.longitude, // null si se cumple el timeout
                    provider = LocationManager.GPS_PROVIDER,
                    altitude = if (sensorLoc?.hasAltitude() == true) sensorLoc.altitude else null,
                    timestamp = now
                )
            )
        } catch (e: Exception) {
            // Failsafe pasivo
        }
    }

    // Adaptador suspendible para emular el flujo lineal sobre la API legacy de LocationManager
    @SuppressLint("MissingPermission")
    private suspend fun getRawGpsLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val listener = android.location.LocationListener { location ->
            if (continuation.isActive) continuation.resume(location, null)
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener,
                mainLooper
            )
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resumeWith(Result.failure(e))
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        captureJob?.cancel() // Cancela de inmediato el bucle de la corutina al apagar el servicio
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Captura GNSS Activa")
            .setContentText("Registrando coordenadas en paralelo cada 10s...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio GNSS",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}