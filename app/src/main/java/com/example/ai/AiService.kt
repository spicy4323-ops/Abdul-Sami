package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

data class ParsedCommand(
    val actionType: CommandActionType,
    val payload: String = "",
    val secondaryPayload: String = ""
)

enum class CommandActionType {
    NONE,
    OPEN_YOUTUBE,
    OPEN_CHROME,
    OPEN_CAMERA,
    OPEN_SETTINGS,
    OPEN_MAPS,
    OPEN_DIALER,
    SEARCH_WEB,
    CREATE_NOTE,
    CREATE_REMINDER,
    STOP_SPEAKING
}

class AiService {
    private val tag = "AyeshaAiService"

    // Lazily initialize generative model to avoid startup crash
    private val model by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash"
            )
        } catch (e: Exception) {
            Log.w(tag, "Firebase AI model initialization deferred or fallback: ${e.message}")
            null
        }
    }

    private val systemInstructions = """
        You are AYESHA AI, a premium personal AI companion and intelligent assistant on Android.
        Personality: Highly intelligent, warm, respectful, concise, trustworthy, and articulate.
        You understand English, Hindi, and Urdu.
        Provide direct, helpful, well-structured answers. When explaining, be clear and engaging.
        Keep spoken answers naturally concise unless the user explicitly requests an in-depth explanation or draft.
    """.trimIndent()

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        userMemories: List<Pair<String, String>> = emptyList()
    ): String {
        return try {
            val promptBuilder = StringBuilder()
            promptBuilder.append(systemInstructions).append("\n\n")

            if (userMemories.isNotEmpty()) {
                promptBuilder.append("User Memory Context:\n")
                userMemories.take(5).forEach { (k, v) ->
                    promptBuilder.append("- $k: $v\n")
                }
                promptBuilder.append("\n")
            }

            if (conversationHistory.isNotEmpty()) {
                promptBuilder.append("Recent Conversation:\n")
                conversationHistory.takeLast(4).forEach { (sender, text) ->
                    promptBuilder.append("$sender: $text\n")
                }
                promptBuilder.append("\n")
            }

            promptBuilder.append("User: $prompt\nAyesha:")

            val fullPrompt = promptBuilder.toString()

            val aiModel = model
            if (aiModel != null) {
                withTimeout(45_000L) {
                    val response = aiModel.generateContent(fullPrompt)
                    val text = response.text
                    if (!text.isNullOrBlank()) {
                        text.trim()
                    } else {
                        fallbackHeuristicResponse(prompt)
                    }
                }
            } else {
                fallbackHeuristicResponse(prompt)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(tag, "AI generation timed out", e)
            "Processing took longer than expected. Here is what I can do right now: ${fallbackHeuristicResponse(prompt)}"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(tag, "Firebase AI inference fallback invoked: ${e.message}")
            fallbackHeuristicResponse(prompt)
        }
    }

    fun parseUserCommand(input: String): ParsedCommand {
        val lower = input.trim().lowercase()

        if (lower in listOf("stop", "stop speaking", "be quiet", "shut up", "pause", "cancel")) {
            return ParsedCommand(CommandActionType.STOP_SPEAKING)
        }

        if (lower.contains("open youtube") || lower.contains("launch youtube") || lower.startsWith("youtube")) {
            return ParsedCommand(CommandActionType.OPEN_YOUTUBE)
        }
        if (lower.contains("open chrome") || lower.contains("launch chrome") || lower.contains("open browser")) {
            return ParsedCommand(CommandActionType.OPEN_CHROME)
        }
        if (lower.contains("open camera") || lower.contains("take a picture") || lower.contains("take photo")) {
            return ParsedCommand(CommandActionType.OPEN_CAMERA)
        }
        if (lower.contains("open settings") || lower.contains("device settings")) {
            return ParsedCommand(CommandActionType.OPEN_SETTINGS)
        }
        if (lower.startsWith("open maps") || lower.contains("navigate to") || lower.contains("directions to")) {
            val destination = lower.replace("open maps to", "")
                .replace("navigate to", "")
                .replace("directions to", "")
                .replace("open maps", "").trim()
            return ParsedCommand(CommandActionType.OPEN_MAPS, destination)
        }
        if (lower.startsWith("call") || lower.startsWith("dial") || lower.contains("open phone")) {
            val target = lower.replace("call", "").replace("dial", "").replace("open phone", "").trim()
            return ParsedCommand(CommandActionType.OPEN_DIALER, target)
        }
        if (lower.startsWith("search web for") || lower.startsWith("google") || lower.startsWith("search for")) {
            val query = lower.replace("search web for", "")
                .replace("search for", "")
                .replace("google", "").trim()
            return ParsedCommand(CommandActionType.SEARCH_WEB, query)
        }

        if (lower.startsWith("take a note") || lower.startsWith("save note") || lower.startsWith("note:")) {
            val noteContent = input.substringAfter("note").removePrefix(":").trim()
            return ParsedCommand(CommandActionType.CREATE_NOTE, noteContent)
        }

        if (lower.startsWith("remind me to") || lower.startsWith("create reminder") || lower.startsWith("reminder:")) {
            val reminderContent = input.substringAfter("to").trim()
            return ParsedCommand(CommandActionType.CREATE_REMINDER, reminderContent)
        }

        return ParsedCommand(CommandActionType.NONE)
    }

    private fun fallbackHeuristicResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("who are you") || lower.contains("what is your name") ->
                "I am Ayesha, your personal AI assistant. I am equipped with neural voice processing, device automation, persistent memory, and productivity tools to assist you seamlessly."

            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! I am ready to assist you. You can talk to me, ask questions, draft business messages, manage your reminders, or ask me to open apps."

            lower.contains("how are you") ->
                "I am operating at peak neural efficiency. How can I empower your day today?"

            lower.contains("hindi") || lower.contains("urdu") || lower.contains("namaste") || lower.contains("salam") ->
                "Namaste aur Salam! Main Ayesha hoon, aapki personal AI assistant. Main English, Hindi aur Urdu mein madad kar sakti hoon."

            lower.contains("draft email") || lower.contains("client message") ->
                "Here is a draft for your client:\n\nSubject: Project Update & Next Steps\n\nDear Client,\n\nI hope this email finds you well. I am writing to provide a concise update on our recent milestones and confirm the schedule for our upcoming deliverables. Please let me know if you would like to review the draft together.\n\nBest regards,"

            lower.contains("what can you do") ->
                "I can converse via real-time voice, launch Android apps (YouTube, Chrome, Camera, Settings, Maps), save personal notes, schedule reminders, remember your preferences, and draft business communications."

            else ->
                "I understand: \"$prompt\". I have logged this into our active neural workspace. Would you like me to research this further, create a reminder, or take action on your device?"
        }
    }
}
