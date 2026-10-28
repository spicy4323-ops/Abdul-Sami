package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSpeaking
import com.example.voice.SpeechState
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    speechState: SpeechState,
    amplitude: Float,
    barCount: Int = 24,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val activeColor = when (speechState) {
        SpeechState.SPEAKING -> EmeraldSpeaking
        SpeechState.LISTENING -> ElectricCyan
        SpeechState.THINKING -> CyberViolet
        else -> ElectricCyan.copy(alpha = 0.5f)
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val maxHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceAtLeast(3f)
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount + 1)

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            // Harmonic wave formula
            val wave = (sin(phase + normalizedIndex * 4f) + 1f) / 2f
            val baseFactor = if (speechState == SpeechState.IDLE) 0.15f else 0.3f
            val dynamicHeight = maxHeight * (baseFactor + wave * amplitude * 0.7f).coerceIn(0.12f, 0.95f)

            val x = spacing + i * (barWidth + spacing)
            val y = (maxHeight - dynamicHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeColor,
                        CyberViolet.copy(alpha = 0.8f)
                    ),
                    startY = y,
                    endY = y + dynamicHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
