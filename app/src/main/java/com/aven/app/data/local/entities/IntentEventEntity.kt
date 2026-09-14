package com.aven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intent_events")
data class IntentEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val intentType: String,
    val isIntentional: Boolean,
    val continued: Boolean,
    val pauseDurationSeconds: Int = 3
)
