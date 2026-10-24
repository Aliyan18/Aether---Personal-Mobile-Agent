package com.aether.agent.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aether.agent.memory.EpisodeDatabase
import com.aether.agent.memory.EpisodeEntity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { EpisodeDatabase.getInstance(context) }
    val dao = remember { database.episodeDao() }
    
    var episodes by remember { mutableStateOf<List<EpisodeEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // Load episodes
    LaunchedEffect(Unit) {
        isLoading = true
        episodes = dao.getRecentEpisodes()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Data Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📝 ${episodes.size} actions recorded")
                    Text("💾 ~${calculateDatabaseSize(context)} KB stored")
                    Text("📅 Oldest: ${getOldestDate(episodes)}")
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            exportData(context, episodes)
                            Toast.makeText(context, "Exported to internal storage", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤 Export JSON")
                }
                
                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("🗑️ Delete All")
                }
            }
            
            HorizontalDivider()
            
            Text("Your Action History", style = MaterialTheme.typography.titleMedium)
            
            // History List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (episodes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data recorded yet.\nAether will store actions here as they happen.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(episodes) { episode ->
                        PrivacyHistoryCard(
                            episode = episode,
                            onDelete = {
                                scope.launch {
                                    dao.deleteEpisode(episode.id)
                                    episodes = dao.getRecentEpisodes()
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Delete All Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete All Data?") },
            text = { Text("This will permanently remove all your action history. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dao.deleteAll()
                            episodes = emptyList()
                            Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PrivacyHistoryCard(
    episode: EpisodeEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = episode.actionTaken.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(Date(episode.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "📢 ${episode.triggerText.take(80)}${if (episode.triggerText.length > 80) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ Outcome: ${episode.outcome}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (episode.outcome) {
                        "success" -> MaterialTheme.colorScheme.primary
                        "failure" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this action?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper functions
private fun calculateDatabaseSize(context: android.content.Context): Long {
    val dbFile = context.getDatabasePath("aether_memory.db")
    return if (dbFile.exists()) dbFile.length() / 1024 else 0
}

private fun getOldestDate(episodes: List<EpisodeEntity>): String {
    val oldest = episodes.minByOrNull { it.timestamp }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return oldest?.let { dateFormat.format(Date(it.timestamp)) } ?: "No data"
}

private suspend fun exportData(context: android.content.Context, episodes: List<EpisodeEntity>) {
    val json = episodes.joinToString(",\n", "[", "]") { episode ->
        """
        {
            "id": ${episode.id},
            "trigger": "${episode.triggerText.replace("\"", "\\\"")}",
            "action": "${episode.actionTaken}",
            "outcome": "${episode.outcome}",
            "timestamp": ${episode.timestamp}
        }
        """.trimIndent()
    }
    
    val exportFile = File(context.getExternalFilesDir(null), "Aether_export_${System.currentTimeMillis()}.json")
    exportFile.writeText(json)
}
