package com.aether.agent.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerText: String,           // What user said or what triggered
    val actionTaken: String,           // What agent did (e.g., "mute_phone")
    val outcome: String,               // "success", "failure", "rejected"
    val userCorrection: String? = null, // If user overrode, what they did instead
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,      // Where this happened (e.g., "home")
    val appPackage: String? = null,     // Which app was open
)
