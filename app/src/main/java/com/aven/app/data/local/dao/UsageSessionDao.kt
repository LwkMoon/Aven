package com.aven.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aven.app.data.local.entities.UsageSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSessionDao {
    @Query("SELECT * FROM usage_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<UsageSessionEntity>>

    @Query("SELECT * FROM usage_sessions WHERE startTime >= :sinceTimestamp ORDER BY startTime DESC")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<UsageSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<UsageSessionEntity>)

    @Query("DELETE FROM usage_sessions")
    suspend fun deleteAll()
}
