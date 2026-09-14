package com.aven.app

import com.aven.app.core.behavior.BehaviorEngine
import com.aven.app.core.garden.GardenEngine
import com.aven.app.core.goal.GoalEngine
import com.aven.app.core.intent.IntentEngine
import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.local.entities.IntentEventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AvenCoreLogicTest {

    @Test
    fun testIntentEngineTaxonomy() {
        val options = IntentEngine.allOptions
        assertTrue(options.size >= 7)

        val specific = options.first { it.key == "check_specific" }
        assertTrue(specific.isIntentional)
        assertEquals("Check something specific", specific.label)

        val bored = options.first { it.key == "bored" }
        assertFalse(bored.isIntentional)
        assertEquals("I'm bored", bored.label)

        val found = IntentEngine.findByLabel("Reply to someone")
        assertNotNull(found)
        assertTrue(found?.isIntentional == true)
    }

    @Test
    fun testGardenEngineStageProgression() {
        var state = GardenStateEntity(
            id = 1,
            growthLevel = 0f,
            stage = "SEED",
            environmentState = "resting",
            unlockedElementsRaw = "seed_pod",
            consecutiveHealthyDays = 0,
            totalIntentionalDecisions = 0,
            totalAvoidedDecisions = 0
        )

        // Record an intentional launch
        val intentionalEvent = IntentEventEntity(
            timestamp = System.currentTimeMillis(),
            packageName = "com.instagram.android",
            appName = "Instagram",
            intentType = "Check something specific",
            isIntentional = true,
            continued = true
        )
        state = GardenEngine.applyEvent(state, intentionalEvent)

        assertEquals(1.5f, state.growthLevel, 0.01f)
        assertEquals(1, state.totalIntentionalDecisions)
        assertEquals("balanced", state.environmentState)

        // Advance growth past 20 to trigger SPROUT (minGrowth = 20)
        state = state.copy(growthLevel = 21f)
        state = GardenEngine.applyEvent(state, intentionalEvent)
        assertEquals("SPROUT", state.stage)
        assertEquals("thriving", state.environmentState) // >= 20 and positive delta

        // Advance past 45 to trigger GROVE (minGrowth = 45)
        state = state.copy(growthLevel = 46f)
        state = GardenEngine.applyEvent(state, intentionalEvent)
        assertEquals("GROVE", state.stage)

        // Advance past 70 to trigger HABITAT (minGrowth = 70)
        state = state.copy(growthLevel = 72f)
        state = GardenEngine.applyEvent(state, intentionalEvent)
        assertEquals("HABITAT", state.stage)

        // Advance past 90 to trigger LIVING_WORLD (minGrowth = 90)
        state = state.copy(growthLevel = 92f)
        state = GardenEngine.applyEvent(state, intentionalEvent)
        assertEquals("LIVING_WORLD", state.stage)
    }

    @Test
    fun testGardenEngineSoftDecayAndRecovery() {
        val thrivingState = GardenStateEntity(
            id = 1,
            growthLevel = 50f,
            stage = "GROVE",
            environmentState = "thriving",
            consecutiveHealthyDays = 3
        )

        // After 2 missed days (soft decay of 1.0f per day)
        val decayed = GardenEngine.applySoftDecay(thrivingState, daysDormant = 2)
        assertEquals("resting", decayed.environmentState)
        assertEquals(48f, decayed.growthLevel, 0.01f) // 50 - 2 = 48
        assertEquals("GROVE", decayed.stage) // never lose primary unlocked stage

        // Rejuvenation from conscious choices
        val consciousEvent = IntentEventEntity(
            timestamp = System.currentTimeMillis(),
            packageName = "com.twitter.android",
            appName = "X",
            intentType = "I'm bored",
            isIntentional = false,
            continued = false // Avoided!
        )
        val recovered = GardenEngine.applyEvent(decayed, consciousEvent)
        assertEquals("thriving", recovered.environmentState)
        assertTrue(recovered.growthLevel > decayed.growthLevel)
    }

    @Test
    fun testBehaviorEngineAggregationAndDerivation() {
        val now = System.currentTimeMillis()
        val events = listOf(
            IntentEventEntity(timestamp = now, packageName = "com.instagram.android", appName = "Instagram", intentType = "Reply to someone", isIntentional = true, continued = true),
            IntentEventEntity(timestamp = now, packageName = "com.instagram.android", appName = "Instagram", intentType = "I'm bored", isIntentional = false, continued = false),
            IntentEventEntity(timestamp = now, packageName = "com.twitter.android", appName = "X", intentType = "I don't know", isIntentional = false, continued = true)
        )

        val summary = BehaviorEngine.aggregateDaily("2026-09-13", events)
        assertEquals(3, summary.interventions)
        assertEquals(1, summary.intentionalLaunches)
        assertEquals(1, summary.mindlessLaunches)
        assertEquals(1, summary.avoidedLaunches)

        val weekly = BehaviorEngine.computeWeeklyStats(events)
        assertEquals(3, weekly.totalInterventions)
        assertEquals(1, weekly.intentionalCount)
        assertEquals(1, weekly.avoidedCount)
        assertEquals(66, weekly.intentionalPercentage) // (1 intentional + 1 avoided) / 3 = 66%

        val breakdowns = BehaviorEngine.computeAppBreakdowns(events)
        assertEquals(2, breakdowns.size)
        val insta = breakdowns.find { it.packageName == "com.instagram.android" }
        assertNotNull(insta)
        assertEquals(2, insta?.totalInterventions)
        assertEquals(100, insta?.intentionalRate) // 1 intentional + 1 avoided = 2/2 = 100%

        val insights = BehaviorEngine.deriveInsights(events)
        assertTrue(insights.isNotEmpty())
    }

    @Test
    fun testGoalEngineEvaluation() {
        val summary = DailySummaryEntity(
            date = "2026-09-13",
            intentionalLaunches = 4,
            mindlessLaunches = 1,
            interventions = 6,
            avoidedLaunches = 1
        )

        val goals = GoalEngine.evaluateGoals(summary, listOf(summary))
        assertEquals(3, goals.size)
        val rateGoal = goals.find { it.id == "goal_intentional_rate" }
        assertNotNull(rateGoal)
        // 5 intentional/avoided out of 6 = 83% > 70% target
        assertTrue(rateGoal!!.isMet)
    }
}
