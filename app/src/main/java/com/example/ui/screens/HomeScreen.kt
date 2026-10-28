package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.NeuralOrbCanvas
import com.example.ui.theme.AmberExecuting
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSpeaking
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AyeshaUiState
import com.example.voice.SpeechState

@Composable
fun HomeScreen(
    uiState: AyeshaUiState,
    onMicClick: () -> Unit,
    onStopSpeech: () -> Unit,
    onSendText: (String) -> Unit,
    onQuickAction: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VoidBlack,
                        Color(0xFF090E1A),
                        Color(0xFF0D1527)
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AYESHA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricCyan)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VoidBlack
                        )
                    }
                }
                Text(
                    text = "Personal Voice Intelligence",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onNavigate(AppScreen.VOICE_FOCUS) },
                    modifier = Modifier.testTag("nav_voice_focus")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Mode",
                        tint = ElectricCyan
                    )
                }
                IconButton(
                    onClick = { onNavigate(AppScreen.CHAT_HISTORY) },
                    modifier = Modifier.testTag("nav_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier.testTag("nav_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Badge Pill
        val statusColor = when (uiState.speechState) {
            SpeechState.IDLE -> ElectricCyan
            SpeechState.LISTENING -> ElectricCyan
            SpeechState.THINKING -> CyberViolet
            SpeechState.SPEAKING -> EmeraldSpeaking
            SpeechState.EXECUTING -> AmberExecuting
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(GlassSurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.statusText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive 3D Reactive Neural Energy Core
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .clickable { onNavigate(AppScreen.VOICE_FOCUS) }
        ) {
            NeuralOrbCanvas(
                speechState = uiState.speechState,
                amplitude = uiState.amplitude,
                isLowEffectsMode = uiState.isLowEffectsMode,
                modifier = Modifier.size(240.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reactive Audio Waveforms
        AudioWaveformVisualizer(
            speechState = uiState.speechState,
            amplitude = uiState.amplitude,
            barCount = 28,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(36.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Microphone Control Dock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.speechState == SpeechState.SPEAKING) {
                IconButton(
                    onClick = onStopSpeech,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceElevated)
                        .testTag("stop_speech_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speech",
                        tint = RubyAlertColor()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Central Mic Button
            val micBgBrush = when (uiState.speechState) {
                SpeechState.LISTENING -> Brush.radialGradient(listOf(ElectricCyan, CyberViolet))
                SpeechState.SPEAKING -> Brush.radialGradient(listOf(EmeraldSpeaking, ElectricCyan))
                SpeechState.THINKING -> Brush.radialGradient(listOf(CyberViolet, Color(0xFF4C1D95)))
                else -> Brush.radialGradient(listOf(ElectricCyan, Color(0xFF0284C7)))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .shadow(16.dp, CircleShape, spotColor = ElectricCyan)
                    .clip(CircleShape)
                    .background(micBgBrush)
                    .clickable { onMicClick() }
                    .testTag("mic_toggle_button")
            ) {
                if (uiState.speechState == SpeechState.THINKING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = VoidBlack,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (uiState.speechState == SpeechState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = VoidBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Chips Scroll
        Text(
            text = "QUICK ACTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(
                icon = Icons.Default.SmartDisplay,
                label = "Open YouTube",
                onClick = { onQuickAction("Open YouTube") }
            )
            QuickChip(
                icon = Icons.Default.NoteAdd,
                label = "Take Note",
                onClick = { onQuickAction("Take a note: Meeting tomorrow at 10 AM") }
            )
            QuickChip(
                icon = Icons.Default.Event,
                label = "Remind Me",
                onClick = { onQuickAction("Remind me to submit project review") }
            )
            QuickChip(
                icon = Icons.Default.Search,
                label = "Search Web",
                onClick = { onQuickAction("Search web for latest AI news") }
            )
            QuickChip(
                icon = Icons.Default.CameraAlt,
                label = "Open Camera",
                onClick = { onQuickAction("Open camera") }
            )
            QuickChip(
                icon = Icons.Default.BusinessCenter,
                label = "Draft Email",
                onClick = { onQuickAction("Draft an email for my client regarding project updates") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Conversation Preview Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderGlow = uiState.speechState == SpeechState.SPEAKING
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LATEST RESPONSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElectricCyan
                    )
                    Text(
                        text = "Tap for chat",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.clickable { onNavigate(AppScreen.CHAT_HISTORY) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.transcript.isNotBlank()) {
                    Text(
                        text = "“${uiState.transcript}”",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = uiState.latestResponse,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Text Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask Ayesha or type a command…", fontSize = 13.sp, color = TextTertiary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("text_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GlassSurface,
                    unfocusedContainerColor = GlassSurface,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendText(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .testTag("send_text_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = VoidBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassSurface)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

private fun RubyAlertColor(): Color = Color(0xFFEF4444)
