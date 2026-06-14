package com.aether.agent.triggers

import android.content.Context
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.memory.MemoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProactiveEngine(
    private val context: Context,
    private val executor: ActionExecutor,
    private val memoryManager: MemoryManager,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Called when we observe a pattern
    suspend fun evaluateProactiveAction(observation: String, confidence: Float) {
        // Check memory: Has user rejected similar actions before?
        val correctionCount = memoryManager.getCorrectionHistory(observation)

        val finalConfidence = if (correctionCount > 0) {
            confidence * (0.9f / (correctionCount + 1)) // Lower confidence if previously rejected
        } else {
            confidence
        }

        when {
            finalConfidence > 0.9f -> {
                // High confidence: auto-execute
                scope.launch {
                    executor.execute(observation, emptyMap())
                    // Notify user with a subtle toast
                }
            }
            finalConfidence > 0.7f -> {
                // Medium confidence: ask for approval
                showApprovalDialog(observation)
            }
            else -> {
                // Low confidence: just log
                println("Proactive: Ignoring $observation (confidence $finalConfidence)")
            }
        }
    }

    private fun showApprovalDialog(action: String) {
        // In real implementation, show a system notification asking "Allow Aether to $action?"
        // For now, just log
        println("Would you like to: $action?")
    }
}
