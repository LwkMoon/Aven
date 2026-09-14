package com.aven.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aven.app.data.local.dao.DailySummaryDao
import com.aven.app.data.local.dao.GardenStateDao
import com.aven.app.data.local.dao.IntentEventDao
import com.aven.app.data.local.dao.MonitoredAppDao
import com.aven.app.data.local.dao.UsageSessionDao
import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.local.entities.IntentEventEntity
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.data.local.entities.UsageSessionEntity

@Database(
    entities = [
        MonitoredAppEntity::class,
        IntentEventEntity::class,
        UsageSessionEntity::class,
        DailySummaryEntity::class,
        GardenStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AvenDatabase : RoomDatabase() {
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun intentEventDao(): IntentEventDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun gardenStateDao(): GardenStateDao

    companion object {
        @Volatile
        private var INSTANCE: AvenDatabase? = null

        fun getInstance(context: Context): AvenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AvenDatabase::class.java,
                    "aven_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
