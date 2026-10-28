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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.AyeshaUiState
import com.example.voice.VoiceProfile

@Composable
fun SettingsScreen(
    uiState: AyeshaUiState,
    onSelectVoiceProfile: (VoiceProfile) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onToggleLowEffects: (Boolean) -> Unit,
    onPreviewVoice: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "SETTINGS & VOICES",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: VOICE PROFILES
        Text(
            text = "SELECTABLE VOICE PROFILES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Profile A: Ayesha Female
        VoiceProfileOptionCard(
            title = "Ayesha Voice",
            subtitle = "Natural, warm & articulate adult female persona",
            isSelected = uiState.activeVoiceProfile == VoiceProfile.AYESHA_FEMALE,
            onClick = { onSelectVoiceProfile(VoiceProfile.AYESHA_FEMALE) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Profile B: Alternative Male
        VoiceProfileOptionCard(
            title = "Alternative Voice",
            subtitle = "Calm, professional & authoritative male persona",
            isSelected = uiState.activeVoiceProfile == VoiceProfile.ALTERNATIVE_MALE,
            onClick = { onSelectVoiceProfile(VoiceProfile.ALTERNATIVE_MALE) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPreviewVoice,
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassSurface,
                contentColor = ElectricCyan
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preview_voice_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Preview Selected Voice")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: SPEECH TUNING
        Text(
            text = "SPEECH CADENCE & PITCH",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", fontSize = 13.sp, color = TextPrimary)
                    Text(String.format("%.1fx", uiState.speechRate), fontSize = 13.sp, color = ElectricCyan)
                }
                Slider(
                    value = uiState.speechRate,
                    onValueChange = onSpeechRateChange,
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Pitch", fontSize = 13.sp, color = TextPrimary)
                    Text(String.format("%.2fx", uiState.speechPitch), fontSize = 13.sp, color = CyberViolet)
                }
                Slider(
                    value = uiState.speechPitch,
                    onValueChange = onSpeechPitchChange,
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(thumbColor = CyberViolet, activeTrackColor = CyberViolet)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: LANGUAGE
        Text(
            text = "PREFERRED LANGUAGE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val languages = listOf("en" to "English", "hi" to "Hindi (हिंदी)", "ur" to "Urdu (اردو)")
            languages.forEach { (code, label) ->
                val isSelected = uiState.activeLanguage == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ElectricCyan else GlassSurface)
                        .clickable { onLanguageChange(code) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) VoidBlack else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: PERFORMANCE & ACCESSIBILITY
        Text(
            text = "PERFORMANCE & GRAPHICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reduced Visual Effects", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Reduces 3D particle count to conserve battery on modest hardware.", fontSize = 11.sp, color = TextSecondary)
                }
                Switch(
                    checked = uiState.isLowEffectsMode,
                    onCheckedChange = onToggleLowEffects,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidBlack,
                        checkedTrackColor = ElectricCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: PRIVACY & SECURITY
        Text(
            text = "PRIVACY & LOCAL ISOLATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Strict Data Privacy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ayesha processes conversation locally on your device with Room SQLite storage. No audio is recorded secretly or logged without microphone activation.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 6: ACCOUNT
        Text(
            text = "USER ACCOUNT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2D1622),
                contentColor = Color(0xFFEF4444)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("logout_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Out & Disconnect", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun VoiceProfileOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF162544) else GlassSurface)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ElectricCyan else TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }
        }
    }
}
