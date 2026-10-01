package com.smartfactory.ai.presentation.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Une jauge semi-circulaire (comme un compteur de vitesse) pour afficher la température d'une machine.
 * 
 * @param temperature La valeur actuelle (ex: 75.0)
 * @param maxTemperature Le maximum du compteur (ex: 100.0)
 * @param title Le nom du capteur (ex: "Température")
 */
@Composable
fun CircularGaugeView(
    temperature: Float,
    maxTemperature: Float = 100f,
    title: String = "Température"
) {
    // 1. DÉFINITION DE LA COULEUR
    // La couleur change en fonction de la chaleur
    val gaugeColor = when {
        temperature >= 85f -> Color.Red // Critique !
        temperature >= 60f -> Color(0xFFFFA500) // Orange (Attention)
        else -> Color.Green // Vert (Tout va bien)
    }

    // 2. ANIMATION FLUIDE
    // Au lieu que l'aiguille saute d'un coup, on crée une animation douce
    val animatedProgress by animateFloatAsState(
        targetValue = temperature / maxTemperature,
        animationSpec = tween(durationMillis = 1000), // L'animation dure 1 seconde
        label = "gauge_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .aspectRatio(1f) // Pour garder un carré parfait pour le cercle
        ) {
            // 3. LE DESSIN (CANVAS)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = size.width * 0.1f // L'épaisseur du trait = 10% de la taille
                
                // On dessine le fond gris (le compteur vide, 180 degrés = un demi cercle)
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // On dessine la ligne de couleur par dessus (l'aiguille remplie)
                drawArc(
                    color = gaugeColor,
                    startAngle = 180f,
                    sweepAngle = 180f * animatedProgress.coerceIn(0f, 1f), // On limite entre 0 et 100%
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Le texte de la valeur affiché au centre
            Text(
                text = "${temperature.toInt()}°C",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Le titre sous la jauge
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

// 4. LA MAGIE DE COMPOSE : LES PREVIEWS
// Ce code permet de voir le résultat directement dans Android Studio sans lancer d'émulateur !
@Preview(showBackground = true)
@Composable
fun CircularGaugeViewPreviewNormal() {
    Box(modifier = Modifier.height(150.dp)) {
        CircularGaugeView(temperature = 45f) // Sera vert
    }
}

@Preview(showBackground = true)
@Composable
fun CircularGaugeViewPreviewWarning() {
    Box(modifier = Modifier.height(150.dp)) {
        CircularGaugeView(temperature = 75f) // Sera orange
    }
}

@Preview(showBackground = true)
@Composable
fun CircularGaugeViewPreviewCritical() {
    Box(modifier = Modifier.height(150.dp)) {
        CircularGaugeView(temperature = 92f) // Sera rouge
    }
}
