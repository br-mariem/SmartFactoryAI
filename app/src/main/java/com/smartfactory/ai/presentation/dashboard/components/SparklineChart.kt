package com.smartfactory.ai.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Un mini-graphique (Sparkline) pour afficher l'évolution des vibrations en temps réel.
 * 
 * @param data Liste des 30 dernières valeurs de capteur
 * @param lineColor Couleur de la courbe
 * @param title Titre du graphique
 */
@Composable
fun SparklineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF2196F3), // Bleu par défaut
    title: String = "Vibrations"
) {
    Column(modifier = modifier) {
        // Titre du graphique
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // S'il n'y a pas assez de données, on affiche un espace vide
        if (data.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize())
            return@Column
        }

        // Le dessin du graphique
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Prend tout l'espace restant
        ) {
            // 1. Calcul de l'échelle (Minimum et Maximum)
            val maxValue = data.maxOrNull() ?: 1f
            val minValue = data.minOrNull() ?: 0f
            val range = if (maxValue == minValue) 1f else maxValue - minValue

            // Espacement entre chaque point sur l'axe X
            val stepX = size.width / (if (data.size > 1) data.size - 1 else 1).toFloat()

            // 2. Création du chemin (La ligne qui relie les points)
            val path = Path()
            val points = mutableListOf<Offset>()

            data.forEachIndexed { index, value ->
                val x = index * stepX
                // Pour l'axe Y, Android dessine de haut en bas (0 est en haut)
                // On inverse donc la valeur pour que les gros chiffres soient en haut
                val y = size.height - ((value - minValue) / range * size.height)
                
                val point = Offset(x, y)
                points.add(point)

                if (index == 0) {
                    path.moveTo(x, y) // Premier point, on pose le stylo
                } else {
                    path.lineTo(x, y) // On trace un trait vers le point suivant
                }
            }

            // 3. Dessin du dégradé sous la courbe
            // On ferme le chemin vers le bas pour le remplir avec un dégradé élégant
            val fillPath = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.5f), // Translucide en haut
                        Color.Transparent             // Transparent en bas
                    )
                )
            )

            // 4. Dessin de la courbe principale (Le trait)
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// Aperçu en direct dans Android Studio
@Preview(showBackground = true)
@Composable
fun SparklineChartPreview() {
    // Fausse liste de données pour la démo
    val fakeData = listOf(10f, 15f, 12f, 20f, 25f, 22f, 30f, 28f, 35f, 40f, 38f, 36f, 45f)
    
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(16.dp)
    ) {
        SparklineChart(data = fakeData)
    }
}
