package com.aven.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aven.app.data.local.entities.GardenStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenStateDao {
    @Query("SELECT * FROM garden_state WHERE id = 1 LIMIT 1")
    fun getGardenState(): Flow<GardenStateEntity?>

    @Query("SELECT * FROM garden_state WHERE id = 1 LIMIT 1")
    suspend fun getGardenStateSnapshot(): GardenStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGardenState(state: GardenStateEntity)

    @Update
    suspend fun updateGardenState(state: GardenStateEntity)

    @Query("DELETE FROM garden_state")
    suspend fun deleteAll()
}
