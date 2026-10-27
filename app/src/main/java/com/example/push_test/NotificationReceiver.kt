package com.example.push_test

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)
        helper.showNotification("¡Alarma Background!", "Esta notificación cíclica acaba de sonar.")
        // Auto-reprogramar para seguir el ciclo infinito cada 5 segundos
        scheduleNext(context)
    }

    companion object {
        // Genera el intent estándar que usamos para crear o cancelar alarmas
        private fun getPendingIntent(context: Context, flags: Int): PendingIntent? {
            val intent = Intent(context, NotificationReceiver::class.java)
            // FLAG_IMMUTABLE es obligatorio desde Android 12 por seguridad.
            return PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_IMMUTABLE)
        }

        fun scheduleNext(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // FLAG_UPDATE_CURRENT: Si ya existe, actualízalo.
            val pendingIntent = getPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)

            val timeInMillis = System.currentTimeMillis() + 5000 // 5 segundos en el futuro

            try {
                // setExactAndAllowWhileIdle garantiza que se ejecute a la hora exacta incluso si el teléfono entra en reposo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent!!)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent!!)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        // NUEVO: Función para detener el ciclo
        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // Obtenemos la referencia de la alarma exacta que programamos
            val pendingIntent = getPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent) // La eliminamos del sistema
                pendingIntent.cancel()
            }
        }

        // NUEVO: Función para saber si la alarma está corriendo.
        // Usa FLAG_NO_CREATE: si existe devuelve el intent, si no, devuelve null (sin crear uno nuevo)
        fun isAlarmScheduled(context: Context): Boolean {
            return getPendingIntent(context, PendingIntent.FLAG_NO_CREATE) != null
        }

        // Verifica si el sistema nos permite agendar alarmas exactas (Android 12+)
        fun canScheduleExactAlarms(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        }
    }
}