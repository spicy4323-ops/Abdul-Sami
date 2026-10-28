package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberExecuting
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSpeaking
import com.example.voice.SpeechState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class NeuralParticle(
    val orbitRadiusFraction: Float,
    val angleOffset: Float,
    val speed: Float,
    val tiltAngle: Float,
    val isCyan: Boolean,
    val size: Float
)

@Composable
fun NeuralOrbCanvas(
    speechState: SpeechState,
    amplitude: Float,
    isLowEffectsMode: Boolean = false,
    modifier: Modifier = Modifier.size(240.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuralOrbAnimation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (speechState == SpeechState.THINKING) 3500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Pre-calculate fixed particle seeds for performance
    val particleCount = if (isLowEffectsMode) 16 else 36
    val particles = remember(particleCount) {
        List(particleCount) { i ->
            val fraction = 0.35f + (i % 6) * 0.11f
            NeuralParticle(
                orbitRadiusFraction = fraction,
                angleOffset = (i * (360f / particleCount)),
                speed = if (i % 2 == 0) 1.0f else -0.8f,
                tiltAngle = (30f + (i * 25f) % 60f) * (PI / 180f).toFloat(),
                isCyan = i % 2 == 0,
                size = 2.5f + (i % 4) * 1.2f
            )
        }
    }

    val primaryColor = when (speechState) {
        SpeechState.IDLE -> ElectricCyan
        SpeechState.LISTENING -> ElectricCyan
        SpeechState.THINKING -> CyberViolet
        SpeechState.SPEAKING -> EmeraldSpeaking
        SpeechState.EXECUTING -> AmberExecuting
    }

    val secondaryColor = when (speechState) {
        SpeechState.IDLE -> CyberViolet
        SpeechState.LISTENING -> Color(0xFF67E8F9)
        SpeechState.THINKING -> Color(0xFFC084FC)
        SpeechState.SPEAKING -> ElectricCyan
        SpeechState.EXECUTING -> Color(0xFFFBBF24)
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 2f) * 0.85f

        // Dynamic scale responding to amplitude and speech state
        val amplitudeBoost = (amplitude * 0.45f).coerceIn(0f, 0.45f)
        val effectiveRadius = baseRadius * breathingPulse * (1f + amplitudeBoost)

        // 1. Atmospheric Ambient Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f + amplitude * 0.25f),
                    secondaryColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = effectiveRadius * 1.35f
            ),
            radius = effectiveRadius * 1.35f,
            center = center
        )

        // 2. Orbital Cybernetic Rings (Tilted 3D Ellipses)
        val ringCount = if (isLowEffectsMode) 2 else 4
        for (r in 0 until ringCount) {
            val ringRadius = effectiveRadius * (0.55f + r * 0.16f)
            val ringAlpha = 0.25f + (r * 0.1f)
            val ringTilt = (rotationAngle * (if (r % 2 == 0) 1f else -1f) + r * 45f) * (PI / 180f).toFloat()

            drawTiltedRing(
                center = center,
                radiusX = ringRadius,
                radiusY = ringRadius * 0.45f,
                tiltAngle = ringTilt,
                color = if (r % 2 == 0) primaryColor else secondaryColor,
                alpha = ringAlpha
            )
        }

        // 3. Central 3D Energy Core
        val coreRadius = effectiveRadius * 0.38f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    primaryColor.copy(alpha = 0.9f),
                    secondaryColor.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius
            ),
            radius = coreRadius,
            center = center
        )

        // 4. Rotating Signal Particles
        particles.forEach { p ->
            val particleAngle = (rotationAngle * p.speed + p.angleOffset) * (PI / 180f).toFloat()
            val orbitR = effectiveRadius * p.orbitRadiusFraction

            // 3D projection on tilted orbital plane
            val rawX = orbitR * cos(particleAngle)
            val rawY = orbitR * sin(particleAngle) * 0.45f

            // Rotate by tiltAngle
            val projX = center.x + rawX * cos(p.tiltAngle) - rawY * sin(p.tiltAngle)
            val projY = center.y + rawX * sin(p.tiltAngle) + rawY * cos(p.tiltAngle)

            // Depth calculation: particles closer to camera are brighter and larger
            val depth = (sin(particleAngle) + 1f) / 2f
            val particleAlpha = (0.35f + depth * 0.65f).coerceIn(0.2f, 1.0f)
            val pColor = if (p.isCyan) primaryColor else secondaryColor

            drawCircle(
                color = pColor.copy(alpha = particleAlpha),
                radius = p.size * (0.8f + depth * 0.5f),
                center = Offset(projX, projY)
            )

            // Particle connection lines to core when thinking or speaking
            if (!isLowEffectsMode && (speechState == SpeechState.THINKING || speechState == SpeechState.SPEAKING) && depth > 0.7f) {
                drawLine(
                    color = pColor.copy(alpha = 0.25f * depth),
                    start = center,
                    end = Offset(projX, projY),
                    strokeWidth = 1.2f
                )
            }
        }

        // 5. High-intensity Core Sparkle
        drawCircle(
            color = Color.White,
            radius = coreRadius * 0.28f * breathingPulse,
            center = center
        )
    }
}

private fun DrawScope.drawTiltedRing(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    tiltAngle: Float,
    color: Color,
    alpha: Float
) {
    val steps = 40
    val points = mutableListOf<Offset>()

    for (i in 0..steps) {
        val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
        val x = radiusX * cos(theta)
        val y = radiusY * sin(theta)

        val rotatedX = center.x + x * cos(tiltAngle) - y * sin(tiltAngle)
        val rotatedY = center.y + x * sin(tiltAngle) + y * cos(tiltAngle)
        points.add(Offset(rotatedX, rotatedY))
    }

    for (i in 0 until points.size - 1) {
        drawLine(
            color = color.copy(alpha = alpha),
            start = points[i],
            end = points[i + 1],
            strokeWidth = 1.8f
        )
    }
}
