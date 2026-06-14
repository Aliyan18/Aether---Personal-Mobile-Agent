package com.aether.agent.memory

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoryManager(context: Context) {
    private val database = EpisodeDatabase.getInstance(context)
    private val dao = database.episodeDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Store a new memory
    fun remember(episode: EpisodeEntity) {
        scope.launch {
            dao.insert(episode)
            // Keep only last 30 days of memory
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            dao.deleteOldEpisodes(thirtyDaysAgo)
        }
    }

    // Recall similar past events
    suspend fun recallSimilar(trigger: String): List<EpisodeEntity> {
        return dao.searchByKeyword(trigger)
    }

    // Get learning: Did user correct this action before?
    suspend fun getCorrectionHistory(action: String): Int {
        val episodes = dao.getRecentEpisodes()
        return episodes.count { (it.actionTaken == action) && (it.userCorrection != null) }
    }
}
