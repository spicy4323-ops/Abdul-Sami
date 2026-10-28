package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BusinessAssistantScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.RemindersNotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceFocusScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.AppScreen
import com.example.viewmodel.AyeshaViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AyeshaApp(
    viewModel: AyeshaViewModel,
    onRequestRecordAudioPermission: () -> Unit
) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = FirebaseAuth.getInstance().currentUser }
        )
    } else {
        AppContent(
            viewModel = viewModel,
            onRequestRecordAudioPermission = onRequestRecordAudioPermission
        )
    }
}

@Composable
fun AppContent(
    viewModel: AyeshaViewModel,
    onRequestRecordAudioPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VoidBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.currentScreen != AppScreen.VOICE_FOCUS) {
                NavigationBar(
                    containerColor = Color(0xFF090E18),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("ayesha_bottom_nav")
                ) {
                    val navItems = listOf(
                        Triple(AppScreen.HOME, "Home", Icons.Default.Home),
                        Triple(AppScreen.VOICE_FOCUS, "Voice", Icons.Default.GraphicEq),
                        Triple(AppScreen.REMINDERS_NOTES, "Tasks", Icons.Default.EventNote),
                        Triple(AppScreen.MEMORY, "Memory", Icons.Default.Psychology),
                        Triple(AppScreen.BUSINESS, "Business", Icons.Default.BusinessCenter),
                        Triple(AppScreen.SETTINGS, "Settings", Icons.Default.Settings)
                    )

                    navItems.forEach { (screen, label, icon) ->
                        val isSelected = uiState.currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) ElectricCyan else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ElectricCyan else TextTertiary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF132238)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.HOME -> HomeScreen(
                        uiState = uiState,
                        onMicClick = {
                            onRequestRecordAudioPermission()
                            viewModel.toggleVoiceListening()
                        },
                        onStopSpeech = { viewModel.stopSpeaking() },
                        onSendText = { text -> viewModel.processUserMessage(text) },
                        onQuickAction = { actionText -> viewModel.processUserMessage(actionText) },
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )

                    AppScreen.VOICE_FOCUS -> VoiceFocusScreen(
                        uiState = uiState,
                        onMicToggle = {
                            onRequestRecordAudioPermission()
                            viewModel.toggleVoiceListening()
                        },
                        onStopSpeech = { viewModel.stopSpeaking() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.CHAT_HISTORY -> ChatHistoryScreen(
                        messages = chatHistory,
                        onClearHistory = { viewModel.clearChatHistory() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.MEMORY -> MemoryScreen(
                        memories = memories,
                        onAddMemory = { k, v, cat -> viewModel.addMemory(k, v, cat) },
                        onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                        onClearAll = { viewModel.clearAllMemories() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.REMINDERS_NOTES -> RemindersNotesScreen(
                        notes = notes,
                        reminders = reminders,
                        onCreateNote = { title, content -> viewModel.createNote(title, content) },
                        onDeleteNote = { id -> viewModel.deleteNote(id) },
                        onCreateReminder = { title, due -> viewModel.createReminder(title, due) },
                        onToggleReminder = { id, done -> viewModel.toggleReminder(id, done) },
                        onDeleteReminder = { id -> viewModel.deleteReminder(id) },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.BUSINESS -> BusinessAssistantScreen(
                        onGenerate = { prompt -> viewModel.processUserMessage(prompt) },
                        latestDraft = uiState.latestResponse,
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        uiState = uiState,
                        onSelectVoiceProfile = { profile -> viewModel.setVoiceProfile(profile) },
                        onSpeechRateChange = { rate -> viewModel.setSpeechSettings(rate, uiState.speechPitch) },
                        onSpeechPitchChange = { pitch -> viewModel.setSpeechSettings(uiState.speechRate, pitch) },
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        onToggleLowEffects = { enabled -> viewModel.toggleLowEffectsMode(enabled) },
                        onPreviewVoice = { viewModel.previewVoice() },
                        onLogout = { viewModel.logout() },
                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }
        }
    }

    // Safety Confirmation Dialog for sensitive Android Actions
    uiState.pendingConfirmation?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingAction() },
            title = {
                Text(
                    text = "Confirm Action: ${pending.actionTitle}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = pending.details,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = VoidBlack)
                ) {
                    Text("Execute Action")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPendingAction() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = GlassSurfaceElevated
        )
    }
}
