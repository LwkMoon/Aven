package com.aven.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aven.app.data.local.entities.MonitoredAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredAppDao {
    @Query("SELECT * FROM monitored_apps ORDER BY displayName ASC")
    fun getAllApps(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps WHERE enabled = 1 ORDER BY displayName ASC")
    fun getEnabledApps(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): MonitoredAppEntity?

    @Query("SELECT COUNT(*) FROM monitored_apps")
    suspend fun getAppCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<MonitoredAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: MonitoredAppEntity)

    @Update
    suspend fun updateApp(app: MonitoredAppEntity)

    @Query("UPDATE monitored_apps SET enabled = :enabled WHERE packageName = :packageName")
    suspend fun setAppEnabled(packageName: String, enabled: Boolean)

    @Query("DELETE FROM monitored_apps")
    suspend fun deleteAll()
}
