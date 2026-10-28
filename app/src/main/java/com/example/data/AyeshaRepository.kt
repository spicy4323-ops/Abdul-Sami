package com.example.data

import com.example.data.model.ChatMessageEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.NoteEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

class AyeshaRepository(private val database: AyeshaDatabase) {
    val allMemories: Flow<List<MemoryEntity>> = database.memoryDao().getAllMemories()
    val allNotes: Flow<List<NoteEntity>> = database.noteDao().getAllNotes()
    val allReminders: Flow<List<ReminderEntity>> = database.reminderDao().getAllReminders()
    val allChatMessages: Flow<List<ChatMessageEntity>> = database.chatDao().getAllMessages()

    suspend fun getRecentMessages(limit: Int = 10): List<ChatMessageEntity> {
        return database.chatDao().getRecentMessages(limit)
    }

    suspend fun saveMessage(sender: String, text: String, actionType: String? = null) {
        database.chatDao().insertMessage(
            ChatMessageEntity(
                sender = sender,
                text = text,
                actionType = actionType
            )
        )
    }

    suspend fun clearChatHistory() {
        database.chatDao().clearHistory()
    }

    suspend fun addMemory(key: String, value: String, category: String = "preference") {
        database.memoryDao().insertMemory(
            MemoryEntity(
                key = key,
                value = value,
                category = category
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        database.memoryDao().deleteById(id)
    }

    suspend fun clearMemories() {
        database.memoryDao().clearAll()
    }

    suspend fun addNote(title: String, content: String, category: String = "general") {
        database.noteDao().insertNote(
            NoteEntity(
                title = title,
                content = content,
                category = category
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        database.noteDao().deleteById(id)
    }

    suspend fun addReminder(title: String, dueTimeMillis: Long, priority: String = "medium") {
        database.reminderDao().insertReminder(
            ReminderEntity(
                title = title,
                dueTimeMillis = dueTimeMillis,
                priority = priority
            )
        )
    }

    suspend fun toggleReminder(id: Long, isCompleted: Boolean) {
        database.reminderDao().setCompleted(id, isCompleted)
    }

    suspend fun deleteReminder(id: Long) {
        database.reminderDao().deleteById(id)
    }
}
