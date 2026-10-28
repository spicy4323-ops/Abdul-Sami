package com.example.viewmodel

import android.app.Application
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionResult
import com.example.actions.DeviceActionEngine
import com.example.ai.AiService
import com.example.ai.CommandActionType
import com.example.ai.ParsedCommand
import com.example.data.AyeshaDatabase
import com.example.data.AyeshaRepository
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.NoteEntity
import com.example.data.model.ReminderEntity
import com.example.voice.SpeechState
import com.example.voice.VoiceManager
import com.example.voice.VoiceProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    VOICE_FOCUS,
    CHAT_HISTORY,
    MEMORY,
    REMINDERS_NOTES,
    BUSINESS,
    SETTINGS
}

data class AyeshaUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val speechState: SpeechState = SpeechState.IDLE,
    val amplitude: Float = 0.1f,
    val statusText: String = "Ready to assist",
    val transcript: String = "",
    val latestResponse: String = "Hello, I am Ayesha. How can I empower your day today?",
    val pendingConfirmation: ActionResult.RequiresConfirmation? = null,
    val activeVoiceProfile: VoiceProfile = VoiceProfile.AYESHA_FEMALE,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.05f,
    val activeLanguage: String = "en",
    val isLowEffectsMode: Boolean = false,
    val snackbarMessage: String? = null
)

class AyeshaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AyeshaRepository
    private val voiceManager: VoiceManager
    private val actionEngine: DeviceActionEngine
    private val aiService: AiService
    private val credentialManager = CredentialManager.create(application)

    private val _uiState = MutableStateFlow(AyeshaUiState())
    val uiState: StateFlow<AyeshaUiState> = _uiState.asStateFlow()

    val memories: StateFlow<List<MemoryEntity>>
    val notes: StateFlow<List<NoteEntity>>
    val reminders: StateFlow<List<ReminderEntity>>
    val chatHistory: StateFlow<List<ChatMessageEntity>>

    init {
        val database = AyeshaDatabase.getDatabase(application)
        repository = AyeshaRepository(database)
        actionEngine = DeviceActionEngine(application)
        aiService = AiService()
        voiceManager = VoiceManager(application, viewModelScope)

        memories = repository.allMemories.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        notes = repository.allNotes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        reminders = repository.allReminders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        chatHistory = repository.allChatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Observe voice manager states
        viewModelScope.launch {
            voiceManager.speechState.collect { state ->
                val status = when (state) {
                    SpeechState.IDLE -> "Ready to assist"
                    SpeechState.LISTENING -> "Listening to your voice…"
                    SpeechState.THINKING -> "Processing neural response…"
                    SpeechState.SPEAKING -> "Speaking…"
                    SpeechState.EXECUTING -> "Executing device action…"
                }
                _uiState.update { it.copy(speechState = state, statusText = status) }
            }
        }

        viewModelScope.launch {
            voiceManager.amplitude.collect { amp ->
                _uiState.update { it.copy(amplitude = amp) }
            }
        }

        viewModelScope.launch {
            voiceManager.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    _uiState.update { it.copy(transcript = text) }
                }
            }
        }

        viewModelScope.launch {
            voiceManager.errorMessage.collect { err ->
                if (err != null) {
                    showSnackbar(err)
                }
            }
        }

        // Populate initial memories if empty
        viewModelScope.launch {
            if (database.memoryDao().getAllMemories().stateIn(viewModelScope).value.isEmpty()) {
                repository.addMemory("Assistant Name", "Ayesha AI", "system")
                repository.addMemory("Interaction Preference", "Fast, direct & conversational", "preference")
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun toggleVoiceListening() {
        if (_uiState.value.speechState == SpeechState.LISTENING) {
            voiceManager.stopListening()
        } else if (_uiState.value.speechState == SpeechState.SPEAKING) {
            voiceManager.stopSpeaking()
        } else {
            voiceManager.startListening { spokenText ->
                processUserMessage(spokenText)
            }
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                // Ignore
            }
            navigateTo(AppScreen.HOME)
        }
    }

    fun processUserMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(transcript = message) }
            repository.saveMessage("USER", message)

            // 1. Check for command actions
            val command = aiService.parseUserCommand(message)
            if (command.actionType != CommandActionType.NONE) {
                handleDeviceCommand(command, message)
                return@launch
            }

            // 2. Process conversation through AI
            voiceManager.setSpeechState(SpeechState.THINKING)
            val recentContext = repository.getRecentMessages(6).map { it.sender to it.text }
            val currentMemories = memories.value.map { it.key to it.value }

            val response = aiService.generateResponse(
                prompt = message,
                conversationHistory = recentContext,
                userMemories = currentMemories
            )

            _uiState.update {
                it.copy(
                    latestResponse = response
                )
            }
            repository.saveMessage("AYESHA", response)

            // Automatically speak response via selected voice profile
            voiceManager.speak(response)
        }
    }

    private suspend fun handleDeviceCommand(command: ParsedCommand, rawMessage: String) {
        voiceManager.setSpeechState(SpeechState.EXECUTING)

        when (command.actionType) {
            CommandActionType.STOP_SPEAKING -> {
                voiceManager.stopSpeaking()
                voiceManager.setSpeechState(SpeechState.IDLE)
            }

            CommandActionType.OPEN_YOUTUBE -> {
                val result = actionEngine.openYouTube()
                handleActionResult(result, rawMessage, "Opening YouTube")
            }

            CommandActionType.OPEN_CHROME -> {
                val result = actionEngine.openChrome()
                handleActionResult(result, rawMessage, "Opening Google Chrome")
            }

            CommandActionType.OPEN_CAMERA -> {
                val result = actionEngine.openCamera()
                handleActionResult(result, rawMessage, "Opening Camera")
            }

            CommandActionType.OPEN_SETTINGS -> {
                val result = actionEngine.openSettings()
                handleActionResult(result, rawMessage, "Opening Device Settings")
            }

            CommandActionType.OPEN_MAPS -> {
                val result = actionEngine.openMaps(command.payload)
                handleActionResult(result, rawMessage, "Opening Maps")
            }

            CommandActionType.OPEN_DIALER -> {
                val result = actionEngine.openDialer(command.payload)
                handleActionResult(result, rawMessage, "Opening phone dialer")
            }

            CommandActionType.SEARCH_WEB -> {
                val query = if (command.payload.isNotBlank()) command.payload else rawMessage
                val result = actionEngine.searchWeb(query)
                handleActionResult(result, rawMessage, "Searching the web for $query")
            }

            CommandActionType.CREATE_NOTE -> {
                val noteText = if (command.payload.isNotBlank()) command.payload else rawMessage
                repository.addNote("Voice Note", noteText, "voice")
                val response = "I have saved your note: \"$noteText\""
                repository.saveMessage("AYESHA", response, "CREATE_NOTE")
                _uiState.update { it.copy(latestResponse = response) }
                voiceManager.speak(response)
                voiceManager.setSpeechState(SpeechState.IDLE)
            }

            CommandActionType.CREATE_REMINDER -> {
                val reminderText = if (command.payload.isNotBlank()) command.payload else "Task"
                val defaultDue = System.currentTimeMillis() + (3600 * 1000 * 24) // Tomorrow
                repository.addReminder(reminderText, defaultDue, "medium")
                val response = "Created reminder: \"$reminderText\" for tomorrow."
                repository.saveMessage("AYESHA", response, "REMINDER")
                _uiState.update { it.copy(latestResponse = response) }
                voiceManager.speak(response)
                voiceManager.setSpeechState(SpeechState.IDLE)
            }

            CommandActionType.NONE -> {
                voiceManager.setSpeechState(SpeechState.IDLE)
            }
        }
    }

    private suspend fun handleActionResult(result: ActionResult, userPrompt: String, actionLabel: String) {
        when (result) {
            is ActionResult.Success -> {
                val speech = result.message
                repository.saveMessage("AYESHA", speech, "ACTION_SUCCESS")
                _uiState.update { it.copy(latestResponse = speech) }
                voiceManager.speak(speech)
                voiceManager.setSpeechState(SpeechState.IDLE)
            }

            is ActionResult.RequiresConfirmation -> {
                _uiState.update { it.copy(pendingConfirmation = result) }
                voiceManager.setSpeechState(SpeechState.IDLE)
            }

            is ActionResult.Error -> {
                val errMsg = "Action could not be executed: ${result.message}"
                repository.saveMessage("AYESHA", errMsg, "ACTION_ERROR")
                _uiState.update { it.copy(latestResponse = errMsg) }
                voiceManager.speak(errMsg)
                voiceManager.setSpeechState(SpeechState.IDLE)
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingConfirmation = null) }
            val result = pending.onConfirm()
            if (result is ActionResult.Success) {
                showSnackbar(result.message)
                voiceManager.speak(result.message)
            } else if (result is ActionResult.Error) {
                showSnackbar(result.message)
            }
        }
    }

    fun dismissPendingAction() {
        _uiState.update { it.copy(pendingConfirmation = null) }
    }

    // Memory operations
    fun addMemory(key: String, value: String, category: String = "preference") {
        viewModelScope.launch {
            repository.addMemory(key, value, category)
            showSnackbar("Saved to Ayesha's memory.")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            showSnackbar("Memory removed.")
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
            showSnackbar("All memories cleared.")
        }
    }

    // Notes operations
    fun createNote(title: String, content: String, category: String = "general") {
        viewModelScope.launch {
            repository.addNote(title, content, category)
            showSnackbar("Note saved.")
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            showSnackbar("Note deleted.")
        }
    }

    // Reminders operations
    fun createReminder(title: String, dueTimeMillis: Long, priority: String = "medium") {
        viewModelScope.launch {
            repository.addReminder(title, dueTimeMillis, priority)
            showSnackbar("Reminder created.")
        }
    }

    fun toggleReminder(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, completed)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
            showSnackbar("Reminder deleted.")
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            showSnackbar("Conversation history cleared.")
        }
    }

    // Settings
    fun setVoiceProfile(profile: VoiceProfile) {
        voiceManager.activeProfile = profile
        voiceManager.applyVoiceSettings()
        _uiState.update { it.copy(activeVoiceProfile = profile) }
        showSnackbar(if (profile == VoiceProfile.AYESHA_FEMALE) "Ayesha Female voice active" else "Alternative Male voice active")
    }

    fun setSpeechSettings(rate: Float, pitch: Float) {
        voiceManager.speechRate = rate
        voiceManager.speechPitch = pitch
        voiceManager.applyVoiceSettings()
        _uiState.update { it.copy(speechRate = rate, speechPitch = pitch) }
    }

    fun setLanguage(languageCode: String) {
        voiceManager.activeLanguageCode = languageCode
        voiceManager.applyVoiceSettings()
        _uiState.update { it.copy(activeLanguage = languageCode) }
        val langName = when (languageCode) {
            "hi" -> "Hindi"
            "ur" -> "Urdu"
            else -> "English"
        }
        showSnackbar("Voice language set to $langName")
    }

    fun toggleLowEffectsMode(enabled: Boolean) {
        _uiState.update { it.copy(isLowEffectsMode = enabled) }
    }

    fun previewVoice() {
        val sample = when (_uiState.value.activeLanguage) {
            "hi" -> "Namaste, main Ayesha hoon. Aapki personal AI sahayak."
            "ur" -> "Salam, main Ayesha hoon. Aapki zaati AI madadgaar."
            else -> "Hello, I am Ayesha. Your personal AI companion on Android."
        }
        voiceManager.speak(sample)
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
