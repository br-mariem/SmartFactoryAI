package com.smartfactory.ai.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfactory.ai.presentation.dashboard.components.CircularGaugeView
import com.smartfactory.ai.presentation.dashboard.components.SparklineChart

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    // On "écoute" les données du ViewModel. Dès qu'elles changent, Compose redessine l'écran.
    val machines by viewModel.machinesState.collectAsState()

    Scaffold(
        topBar = {
            // Une petite barre d'en-tête
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Supervision SmartFactory",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        // LazyVerticalGrid permet de créer une grille (ici 2 colonnes)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(machines) { machine ->
                MachineCard(machine = machine)
            }
        }
    }
}

@Composable
fun MachineCard(machine: MachineUiState) {
    // Card = une carte blanche avec une petite ombre
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Le nom de la machine
            Text(
                text = machine.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // La jauge de température (en haut)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                CircularGaugeView(
                    temperature = machine.temperature,
                    title = "Température"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // La courbe des vibrations (en bas)
            SparklineChart(
                data = machine.vibrations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                title = "Vibrations (mm/s)"
            )
        }
    }
}
