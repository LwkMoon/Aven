package com.aven.app.core.garden

import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.local.entities.IntentEventEntity

/**
 * GardenEngine translates behavioral signals into biological/environmental changes.
 *
 * Growth -> Setback -> Recovery -> Growth.
 * Soft decay: leaves fall, atmosphere quietens, growth slows, but stage progress
 * and unlocked elements are never wiped out punitively.
 */
object GardenEngine {

    enum class GardenStage(
        val displayName: String,
        val minGrowth: Float,
        val maxGrowth: Float,
        val description: String,
        val unlockedFlora: List<String>
    ) {
        SEED(
            displayName = "Seed",
            minGrowth = 0f,
            maxGrowth = 19.9f,
            description = "A quiet patch of fertile soil. A single dormant seed waits for awareness to take root.",
            unlockedFlora = listOf("Seed Pod", "Rich Soil")
        ),
        SPROUT(
            displayName = "Sprout",
            minGrowth = 20f,
            maxGrowth = 44.9f,
            description = "Tender shoots break the surface. Small green leaves unfurl in the morning light.",
            unlockedFlora = listOf("Seed Pod", "Rich Soil", "Tender Sprout", "Morning Moss")
        ),
        GROVE(
            displayName = "Grove",
            minGrowth = 45f,
            maxGrowth = 69.9f,
            description = "Young saplings have taken root. Sunlight filters through a gentle canopy of leaves.",
            unlockedFlora = listOf("Tender Sprout", "Morning Moss", "Silver Birch", "Elder Pine", "Fern Meadow")
        ),
        HABITAT(
            displayName = "Habitat",
            minGrowth = 70f,
            maxGrowth = 89.9f,
            description = "A thriving sanctuary. A winding spring murmurs beside standing stone cairns and sheltering thickets.",
            unlockedFlora = listOf("Elder Pine", "Fern Meadow", "Whispering Brook", "River Stones", "Stone Cairn")
        ),
        LIVING_WORLD(
            displayName = "Living World",
            minGrowth = 90f,
            maxGrowth = 150f,
            description = "An expansive, deeply balanced ecosystem alive with ancient trees, distant mountain vistas, and ambient wildlife.",
            unlockedFlora = listOf("Ancient Redwood", "Wildflower Basin", "Clear Mountain Spring", "Forest Fauna", "Alpine Cairn")
        );

        companion object {
            fun fromGrowth(growth: Float): GardenStage {
                return entries.find { growth >= it.minGrowth && growth <= it.maxGrowth }
                    ?: if (growth > 89.9f) LIVING_WORLD else SEED
            }

            fun fromName(name: String): GardenStage {
                return entries.find { it.name.equals(name, ignoreCase = true) } ?: SEED
            }
        }
    }

    data class EnvironmentalElement(
        val id: String,
        val name: String,
        val stage: GardenStage,
        val lore: String
    )

    val allElements = listOf(
        EnvironmentalElement("seed_pod", "Dormant Seed", GardenStage.SEED, "The origin of intentionality, resting in fertile ground."),
        EnvironmentalElement("rich_soil", "Nourished Earth", GardenStage.SEED, "Layered loam enriched by moments of pause."),
        EnvironmentalElement("tender_sprout", "Tender Sprout", GardenStage.SPROUT, "Broke ground after your first conscious choices."),
        EnvironmentalElement("morning_moss", "Morning Moss", GardenStage.SPROUT, "Grows quietly along the shade of deliberate reflection."),
        EnvironmentalElement("silver_birch", "Silver Birch", GardenStage.GROVE, "Appeared as daily pauses built quiet resilience."),
        EnvironmentalElement("elder_pine", "Elder Pine", GardenStage.GROVE, "Deep roots anchored by sustained awareness across days."),
        EnvironmentalElement("fern_meadow", "Fern Meadow", GardenStage.GROVE, "Unfurled when you stepped away from automatic scrolling."),
        EnvironmentalElement("whispering_brook", "Whispering Brook", GardenStage.HABITAT, "Flowing water bringing tranquility to the sanctuary."),
        EnvironmentalElement("stone_cairn", "Stone Cairn", GardenStage.HABITAT, "Placed by hand to mark consecutive healthy days."),
        EnvironmentalElement("ancient_redwood", "Ancient Redwood", GardenStage.LIVING_WORLD, "A towering testament to enduring intentional habit.")
    )

    /**
     * Applies a new event to the existing GardenState.
     */
    fun applyEvent(
        currentState: GardenStateEntity,
        event: IntentEventEntity
    ): GardenStateEntity {
        // Growth mapping:
        // Avoided launch (Go back): +2.5 growth
        // Intentional continued: +1.5 growth
        // Mindless continued: -0.5 growth (soft setback, clamped to stage floor)
        val growthDelta = when {
            !event.continued -> 2.5f
            event.isIntentional -> 1.5f
            else -> -0.5f // Soft decay
        }

        val currentStage = GardenStage.fromName(currentState.stage)
        // Ensure soft decay does not drop below the current stage floor!
        // Major structural progress is never punitively destroyed.
        val newGrowth = (currentState.growthLevel + growthDelta).coerceAtLeast(currentStage.minGrowth)

        val newStage = GardenStage.fromGrowth(newGrowth)
        val newEnvState = determineEnvironmentState(newGrowth, growthDelta)

        val newIntentional = currentState.totalIntentionalDecisions + if (event.isIntentional && event.continued) 1 else 0
        val newAvoided = currentState.totalAvoidedDecisions + if (!event.continued) 1 else 0

        val unlocked = currentState.unlockedElementsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        newStage.unlockedFlora.forEach { flora ->
            val key = flora.lowercase().replace(" ", "_")
            unlocked.add(key)
        }

        return currentState.copy(
            growthLevel = newGrowth,
            stage = newStage.name,
            environmentState = newEnvState,
            unlockedElementsRaw = unlocked.joinToString(","),
            totalIntentionalDecisions = newIntentional,
            totalAvoidedDecisions = newAvoided,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Determines environmental atmospheric tone (thriving, balanced, resting, dormant).
     */
    private fun determineEnvironmentState(growth: Float, recentDelta: Float): String {
        return when {
            recentDelta > 0 && growth >= 20f -> "thriving"
            recentDelta >= 0 -> "balanced"
            growth < 10f -> "dormant"
            else -> "resting"
        }
    }

    /**
     * Evaluates soft decay after inactivity or heavy mindless days.
     * Never destroys unlocked stages.
     */
    fun applySoftDecay(currentState: GardenStateEntity, daysDormant: Int): GardenStateEntity {
        if (daysDormant <= 1) return currentState
        val decayAmount = (daysDormant * 1.0f).coerceAtMost(8.0f)
        val currentStage = GardenStage.fromName(currentState.stage)
        val decayedGrowth = (currentState.growthLevel - decayAmount).coerceAtLeast(currentStage.minGrowth)
        return currentState.copy(
            growthLevel = decayedGrowth,
            environmentState = "resting",
            consecutiveHealthyDays = 0,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
