package com.aven.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aven.app.data.local.AvenDatabase
import com.aven.app.data.preferences.AvenPreferences
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AvenRepositoryRobolectricTest {

    private lateinit var database: AvenDatabase
    private lateinit var repository: AvenRepository
    private lateinit var preferences: AvenPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AvenDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferences = AvenPreferences(context)
        repository = AvenRepository(database, preferences)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInitializationSeedsDefaults() = runBlocking {
        repository.initializeDefaultsIfNeeded()

        val apps = repository.allMonitoredApps.first()
        assertTrue(apps.isNotEmpty())
        assertTrue(apps.any { it.displayName == "Instagram" })

        val garden = repository.gardenState.first()
        assertNotNull(garden)
        assertEquals("SEED", garden?.stage)
    }

    @Test
    fun testRecordInterventionDecisionUpdatesDatabaseAndGarden() = runBlocking {
        repository.initializeDefaultsIfNeeded()

        repository.recordInterventionDecision(
            packageName = "com.instagram.android",
            appName = "Instagram",
            intentType = "Reply to someone",
            isIntentional = true,
            continued = true,
            pauseDurationSeconds = 3
        )

        val events = repository.allIntentEvents.first()
        assertEquals(1, events.size)
        assertEquals("Instagram", events[0].appName)
        assertTrue(events[0].isIntentional)

        val updatedGarden = repository.gardenState.first()
        assertNotNull(updatedGarden)
        assertTrue((updatedGarden?.growthLevel ?: 0f) > 0f)
        assertEquals(1, updatedGarden?.totalIntentionalDecisions)
    }

    @Test
    fun testGenerateSampleHistoricalDataCreatesRichProgression() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        repository.generateSampleHistoricalData()

        val events = repository.allIntentEvents.first()
        assertTrue("Expected rich events", events.size >= 20)

        val garden = repository.gardenState.first()
        assertEquals("GROVE", garden?.stage)
        assertEquals("thriving", garden?.environmentState)

        val summaries = repository.allDailySummaries.first()
        assertEquals(7, summaries.size)
    }
}
