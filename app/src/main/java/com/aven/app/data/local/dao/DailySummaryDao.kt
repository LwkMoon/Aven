package com.aven.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aven.app.data.local.entities.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries ORDER BY date DESC")
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    suspend fun getSummaryForDate(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    fun observeSummaryForDate(date: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummaries(summaries: List<DailySummaryEntity>)

    @Query("DELETE FROM daily_summaries")
    suspend fun deleteAll()
}
