package com.aether.agent.agent

import android.content.Context
import android.util.Log
import com.aether.agent.memory.EpisodeEntity
import com.aether.agent.memory.MemoryManager
import com.aether.agent.triggers.ProactiveEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgentCore(
    context: Context,
    private val classifier: IntentClassifier,
    private val executor: ActionExecutor,
    private val memoryManager: MemoryManager,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val cloudPlanner = CloudPlanner()
    private val proactiveEngine = ProactiveEngine(context, executor, memoryManager)
    private val tag = "AgentCore"

    fun processTrigger(triggerText: String, isProactive: Boolean = false) {
        scope.launch {
            Log.d(tag, "Processing: $triggerText (proactive=$isProactive)")

            // First try local classifier (fast, offline)
            val localIntent = classifier.classify(triggerText)
            Log.d(tag, "Local classifier result: ${localIntent.action} (${localIntent.confidence})")

            if (localIntent.action != "ignore" && localIntent.confidence > 0.7f) {
                if (isProactive && localIntent.confidence < 0.9f) {
                    // Proactive but medium confidence – ask
                    proactiveEngine.evaluateProactiveAction(localIntent.action, localIntent.confidence)
                } else {
                    val success = executor.execute(localIntent.action, localIntent.params)
                    Log.d(tag, "Local execution of ${localIntent.action}: $success")

                    // Store successful execution
                    if (success) {
                        memoryManager.remember(
                            EpisodeEntity(
                                triggerText = triggerText,
                                actionTaken = localIntent.action,
                                outcome = "success",
                            ),
                        )
                    }
                }
                return@launch
            }

            Log.d(tag, "Local classification failed or ignored, calling CloudPlanner...")
            // If complex, use cloud planner
            cloudPlanner.getPlan(triggerText, object : CloudPlanner.PlanCallback {
                override fun onSuccess(plan: CloudPlanner.Plan) {
                    Log.d(tag, "Cloud plan received: ${plan.steps.size} steps")
                    for (step in plan.steps) {
                        val success = executor.execute(step.action, step.params)
                        Log.d(tag, "Step ${step.action}: ${if (success) "OK" else "FAILED"}")
                    }
                }

                override fun onError(error: String) {
                    Log.e(tag, "Cloud planner error: $error")
                }
            })
        }
    }
}
