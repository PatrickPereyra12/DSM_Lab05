package com.example.amukisense.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class DelayedNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val INPUT_MESSAGE = "input_message"
        private const val CHANNEL_ID = "amukisense_alerts"
    }

    override fun doWork(): Result {
        // Obtenemos el mensaje que nos enviaron desde la pantalla
        val message = inputData.getString(INPUT_MESSAGE) ?: "Mensaje vacío"

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Los canales son obligatorios en las versiones modernas de Android
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Alertas AmukiSense", NotificationManager.IMPORTANCE_HIGH)
        )

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio AmukiSense")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Mostramos la notificación en la pantalla
        manager.notify(System.currentTimeMillis().toInt(), notif)
        return Result.success()
    }
}