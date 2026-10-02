package com.smartfactory.ai.domain.usecase

import android.content.Context
import com.smartfactory.ai.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Le "Veilleur de nuit". Ce Use Case analyse les températures et donne l'alerte.
 */
@Singleton
class DetectAnomalyUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationHelper = NotificationHelper(context)
    
    // Garde en mémoire si une alerte a déjà été envoyée pour ne pas spammer le technicien
    private val alertHistory = mutableSetOf<String>()

    fun checkTemperature(machineId: String, machineName: String, temperature: Float) {
        // La règle métier du cahier des charges : > 85°C
        if (temperature > 85f) {
            // Si on n'a pas encore sonné l'alarme pour cette machine aujourd'hui
            if (!alertHistory.contains(machineId)) {
                // 1. On donne l'alerte sur le téléphone
                notificationHelper.showCriticalAlert(machineName, temperature)
                
                // 2. On marque qu'on a déjà prévenu
                alertHistory.add(machineId)
                
                println("🚨 ALERTE DÉCLENCHÉE POUR : $machineName ($temperature °C)")
            }
        } else if (temperature < 80f) {
            // Si la température redescend à la normale, on "réarme" le système d'alerte
            alertHistory.remove(machineId)
        }
    }
}
