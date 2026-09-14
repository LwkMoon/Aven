package com.aven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val date: String, // Format: yyyy-MM-dd
    val intentionalLaunches: Int,
    val mindlessLaunches: Int,
    val interventions: Int,
    val avoidedLaunches: Int,
    val totalUsageSeconds: Long = 0L
)
