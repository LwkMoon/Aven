package com.aven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "garden_state")
data class GardenStateEntity(
    @PrimaryKey val id: Int = 1,
    val growthLevel: Float = 0f, // 0 to 100+
    val stage: String = "SEED", // SEED, SPROUT, GROVE, HABITAT, LIVING_WORLD
    val environmentState: String = "resting", // thriving, resting, balanced, dormant
    val unlockedElementsRaw: String = "seed_pod,fertile_soil", // comma-separated keys
    val consecutiveHealthyDays: Int = 0,
    val totalIntentionalDecisions: Int = 0,
    val totalAvoidedDecisions: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
