package com.smartfactory.ai.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartfactory.ai.MainActivity
import com.smartfactory.ai.R

/**
 * Ce fichier gère l'envoi d'alertes "Push" sur le téléphone.
 */
class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "factory_alerts_channel"

    init {
        createNotificationChannel()
    }

    // Crée le "Canal" de notifications (obligatoire depuis Android 8.0)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertes Usine"
            val descriptionText = "Notifications critiques des machines de l'usine"
            // IMPORTANCE_HIGH fait apparaître la notification en haut de l'écran avec du son
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // Motif de vibration: Bzz... Bzz...
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Fonction pour déclencher la sirène !
    fun showCriticalAlert(machineName: String, temperature: Float) {
        // Si l'utilisateur clique sur la notification, ça ouvre notre application (MainActivity)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // On construit la notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Petite icône d'alerte Android
            .setContentTitle("🔥 ALERTE CRITIQUE : $machineName")
            .setContentText("Surchauffe détectée : ${temperature.toInt()}°C. Intervention requise !")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Priorité maximale
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // La notification disparaît quand on clique dessus

        // On l'envoie (on vérifie d'abord si on a la permission sur Android 13+)
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                notify(machineName.hashCode(), builder.build())
            }
        }
    }
}
