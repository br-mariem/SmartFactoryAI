package com.smartfactory.ai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import com.smartfactory.ai.domain.usecase.DetectAnomalyUseCase

// Modèle simple pour représenter l'état d'une machine à l'écran
data class MachineUiState(
    val id: String,
    val name: String,
    val temperature: Float,
    val vibrations: List<Float>
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val detectAnomalyUseCase: DetectAnomalyUseCase
) : ViewModel() {

    // L'état de notre écran : une liste de machines
    private val _machinesState = MutableStateFlow<List<MachineUiState>>(emptyList())
    val machinesState: StateFlow<List<MachineUiState>> = _machinesState.asStateFlow()

    init {
        // Au démarrage, on génère 4 fausses machines
        val initialMachines = listOf(
            MachineUiState("M1", "Pompe Hydraulique A", 65f, List(30) { 10f }),
            MachineUiState("M2", "Compresseur B", 45f, List(30) { 15f }),
            MachineUiState("M3", "Moteur Principal", 82f, List(30) { 20f }),
            MachineUiState("M4", "Ventilateur Extraction", 55f, List(30) { 5f })
        )
        _machinesState.value = initialMachines

        // Simulation en temps réel (boucle qui tourne toutes les secondes)
        startSimulation()
    }

    private fun startSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Attendre 1 seconde

                // On met à jour chaque machine avec de nouvelles valeurs aléatoires
                val updatedMachines = _machinesState.value.map { machine ->
                    // Température qui fluctue un peu
                    val newTemp = (machine.temperature + Random.nextFloat() * 4 - 2).coerceIn(20f, 100f)
                    
                    // On ajoute une nouvelle vibration et on garde seulement les 30 dernières
                    val newVibration = Random.nextFloat() * 10 + 10
                    val newVibrationsList = machine.vibrations.drop(1) + newVibration

                    // ---> NOUVEAU (Tâche 08) : On demande au veilleur de nuit de vérifier !
                    detectAnomalyUseCase.checkTemperature(machine.id, machine.name, newTemp)

                    machine.copy(
                        temperature = newTemp,
                        vibrations = newVibrationsList
                    )
                }
                
                // On pousse les nouvelles données vers l'interface graphique
                _machinesState.value = updatedMachines
            }
        }
    }

    // Fonction déclenchée par le gros bouton rouge "Simuler Panne"
    fun injectFailure(machineId: String) {
        val currentMachines = _machinesState.value
        val updatedMachines = currentMachines.map { machine ->
            if (machine.id == machineId) {
                // On monte la température d'un coup à 94°C !
                val fakeTemp = 94f
                // On demande directement au veilleur de nuit de hurler
                detectAnomalyUseCase.checkTemperature(machine.id, machine.name, fakeTemp)
                
                machine.copy(temperature = fakeTemp)
            } else {
                machine
            }
        }
        _machinesState.value = updatedMachines
    }
}
