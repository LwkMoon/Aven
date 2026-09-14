package com.aven.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aven.app.data.local.entities.IntentEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentEventDao {
    @Query("SELECT * FROM intent_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IntentEventEntity>>

    @Query("SELECT * FROM intent_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getEventsSince(sinceTimestamp: Long): Flow<List<IntentEventEntity>>

    @Query("SELECT * FROM intent_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getEventsSinceSnapshot(sinceTimestamp: Long): List<IntentEventEntity>

    @Query("SELECT * FROM intent_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getEventsForApp(packageName: String): Flow<List<IntentEventEntity>>

    @Query("SELECT COUNT(*) FROM intent_events")
    fun getTotalEventCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IntentEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<IntentEventEntity>)

    @Query("DELETE FROM intent_events")
    suspend fun deleteAll()
}
