package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER" or "AYESHA"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null // e.g. "OPEN_APP", "CREATE_NOTE", "REMINDER", null
)
