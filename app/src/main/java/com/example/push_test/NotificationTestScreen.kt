package com.example.push_test

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Space
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun NotificationTestScreen() {
    val context = LocalContext.current
    // Usamos remember para crear el Helper solo una vez y no saturar la memoria en recomposiciones
    val notificationHelper = remember { NotificationHelper(context) }

    // ESTADOS DE LA UI
    // isCycleActive: Guarda si el ciclo está corriendo actualmente.
    // Inicializamos comprobando si ya existe una alarma programada en el sistema.
    var isCycleActive by remember { mutableStateOf(NotificationReceiver.isAlarmScheduled(context)) }

    // timeRemaining: Guarda los segundos faltantes para la próxima notificación.
    var timeRemaining by remember { mutableIntStateOf(5) }

    // Launcher para solicitar el permiso de notificaciones (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso denegado. No verás notificaciones.", Toast.LENGTH_SHORT).show()
        }
    }

    // EFECTO SECUNDARIO PARA EL CONTADOR
    // LaunchedEffect corre una corrutina atada al ciclo de vida de este Composable.
    // Se reinicia automáticamente si cambia el valor de 'isCycleActive'.
    LaunchedEffect(isCycleActive) {
        if (isCycleActive) {
            // Mientras esté activo, restamos 1 cada segundo (1000 ms)
            while (true) {
                delay(1000)
                timeRemaining--
                // Cuando llega a 0, significa que la alarma en background se disparó, así que reiniciamos el reloj visual
                if (timeRemaining <= 0) {
                    timeRemaining = 5
                }
            }
        } else {
            // Si el ciclo se detiene, reiniciamos el reloj a 5
            timeRemaining = 5
        }
    }

    // UI: Diseño de la pantalla
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Texto del contador (Solo se muestra si el ciclo está activo)
        if (isCycleActive) {
            Text(
                text = "Próxima notificación en:",
                fontSize = 18.sp
            )
            Text(
                text = "$timeRemaining s",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        val buttonGeneralStyle = Modifier
            .fillMaxWidth(0.8f)
            .height(55.dp)

        // Botón 1: Instantánea
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF03A9F4), // Color de fondo
                contentColor = Color.White          // Color del texto
            ),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                notificationHelper.showNotification("Notificación Instantanea", "Botón presionado en la UI.")
            },
            modifier = buttonGeneralStyle
        ) {
            Text("Lanzar notificación instantánea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón 2: TOGGLE (Iniciar / Detener)
        Button(
            modifier = buttonGeneralStyle,
            // Cambiamos el color a rojo si está activo para dar feedback visual de "Detener"
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCycleActive) Color.Red else Color(0xFF03A9F4)
            ),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                if (isCycleActive) {
                    // SI ESTÁ ACTIVO -> LO DETENEMOS
                    NotificationReceiver.cancelAlarm(context)
                    isCycleActive = false
                    Toast.makeText(context, "Ciclo detenido", Toast.LENGTH_SHORT).show()
                } else {
                    // SI ESTÁ DETENIDO -> LO INICIAMOS
                    if (NotificationReceiver.canScheduleExactAlarms(context)) {
                        NotificationReceiver.scheduleNext(context)
                        isCycleActive = true
                        Toast.makeText(context, "Ciclo iniciado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Flujo para pedir permisos de alarmas exactas en Android 12/14+
                        Toast.makeText(context, "Requiere permiso de alarmas exactas", Toast.LENGTH_LONG).show()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                        }
                    }
                }
            }
        ) {
            Text(if (isCycleActive) "Detener ciclo de 5s" else "Iniciar ciclo de 5s")
        }
    }
}