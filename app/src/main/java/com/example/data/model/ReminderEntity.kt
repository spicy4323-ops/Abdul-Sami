package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dueTimeMillis: Long,
    val isCompleted: Boolean = false,
    val priority: String = "medium", // "low", "medium", "high"
    val createdAt: Long = System.currentTimeMillis()
)
