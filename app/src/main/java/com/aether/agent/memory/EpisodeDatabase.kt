package com.aether.agent.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EpisodeEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class EpisodeDatabase : RoomDatabase() {
    abstract fun episodeDao(): EpisodeDao

    companion object {
        @Volatile
        private var instance: EpisodeDatabase? = null

        fun getInstance(context: Context): EpisodeDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    EpisodeDatabase::class.java,
                    "aether_memory.db",
                ).build()
                instance = db
                db
            }
        }
    }
}
