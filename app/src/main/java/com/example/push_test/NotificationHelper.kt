package com.example.push_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "prueba_local_id"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    // A partir de Android 8.0 (API 26), todas las notificaciones deben pertenecer a un "Canal".
    // Esto le permite al usuario entrar a los ajustes del teléfono y decidir apagar ciertos canales (ej. Promociones)
    // sin tener que bloquear todas las notificaciones de tu app (ej. Mensajes directos).
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Prueba",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones locales y background"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        // Generar un ID único basado en la hora actual asegura que, si llegan varias notificaciones,
        // no se sobreescriban unas a otras en la barra de estado.
        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Define si hace sonido/vibra
            .setAutoCancel(true) // La notificación desaparece cuando el usuario la toca

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}