package com.aether.agent.triggers

import android.content.Context
import androidx.work.*
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.agent.AgentCore
import com.aether.agent.agent.IntentClassifier
import com.aether.agent.memory.MemoryManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TimeTrigger(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER") agentCore: AgentCore,
) {

    fun scheduleDailyCheck(hour: Int, minute: Int) {
        val workRequest = PeriodicWorkRequestBuilder<TimeTriggerWorker>(
            24,
            TimeUnit.HOURS, // Repeat daily
        ).setInitialDelay(
            calculateInitialDelay(hour, minute),
            TimeUnit.MILLISECONDS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
        }

        return if (target.timeInMillis > now.timeInMillis) {
            target.timeInMillis - now.timeInMillis
        } else {
            target.timeInMillis + (24 * 60 * 60 * 1000) - now.timeInMillis
        }
    }

    fun scheduleRecurringCheck(intervalMinutes: Long) {
        val workRequest = PeriodicWorkRequestBuilder<TimeTriggerWorker>(
            intervalMinutes,
            TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "recurring_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}

// Worker class that runs the actual task
class TimeTriggerWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // This runs in background at scheduled time
        val agentCore = AgentCore(
            applicationContext,
            IntentClassifier(),
            ActionExecutor(applicationContext),
            MemoryManager(applicationContext),
        )

        // Check calendar for upcoming events
        agentCore.processTrigger("Scheduled check: Check calendar for today", isProactive = true)

        return Result.success()
    }
}
