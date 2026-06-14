package com.aether.agent.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EpisodeDao {
    @Insert
    suspend fun insert(episode: EpisodeEntity)

    @Query("SELECT * FROM episodes ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentEpisodes(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE triggerText LIKE :keyword ORDER BY timestamp DESC")
    suspend fun searchByKeyword(keyword: String): List<EpisodeEntity>

    @Query("DELETE FROM episodes WHERE timestamp < :olderThan")
    suspend fun deleteOldEpisodes(olderThan: Long)
}
