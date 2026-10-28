package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.NeuralOrbCanvas
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSpeaking
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.AyeshaUiState
import com.example.voice.SpeechState

@Composable
fun VoiceFocusScreen(
    uiState: AyeshaUiState,
    onMicToggle: () -> Unit,
    onStopSpeech: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        VoidBlack
                    )
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("voice_focus_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = "NEURAL VOICE HUD",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = ElectricCyan
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = uiState.activeLanguage.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large 3D Neural Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            NeuralOrbCanvas(
                speechState = uiState.speechState,
                amplitude = uiState.amplitude,
                isLowEffectsMode = uiState.isLowEffectsMode,
                modifier = Modifier.size(280.dp)
            )
        }

        // Live Audio Waveforms
        AudioWaveformVisualizer(
            speechState = uiState.speechState,
            amplitude = uiState.amplitude,
            barCount = 32,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        // Live Transcript & Assistant Dialogue GlassCard
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderGlow = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.statusText.uppercase(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when (uiState.speechState) {
                        SpeechState.LISTENING -> ElectricCyan
                        SpeechState.SPEAKING -> EmeraldSpeaking
                        SpeechState.THINKING -> CyberViolet
                        else -> TextSecondary
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.transcript.isNotBlank()) {
                    Text(
                        text = "“${uiState.transcript}”",
                        fontSize = 14.sp,
                        color = ElectricCyan,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = uiState.latestResponse,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls Dock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.speechState == SpeechState.SPEAKING) {
                IconButton(
                    onClick = onStopSpeech,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceElevated)
                        .testTag("voice_focus_stop")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speaking",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }

            // Big Pulsing Mic Button
            val micBrush = when (uiState.speechState) {
                SpeechState.LISTENING -> Brush.radialGradient(listOf(ElectricCyan, CyberViolet))
                SpeechState.SPEAKING -> Brush.radialGradient(listOf(EmeraldSpeaking, ElectricCyan))
                SpeechState.THINKING -> Brush.radialGradient(listOf(CyberViolet, Color(0xFF3B0764)))
                else -> Brush.radialGradient(listOf(ElectricCyan, Color(0xFF0369A1)))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(20.dp, CircleShape, spotColor = ElectricCyan)
                    .clip(CircleShape)
                    .background(micBrush)
                    .clickable { onMicToggle() }
                    .testTag("voice_focus_mic")
            ) {
                if (uiState.speechState == SpeechState.THINKING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = VoidBlack,
                        strokeWidth = 3.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (uiState.speechState == SpeechState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic Toggle",
                        tint = VoidBlack,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
