package com.aether.agent.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.agent.AgentCore
import com.aether.agent.agent.IntentClassifier
import com.aether.agent.memory.MemoryManager

class AetherNotificationListener : NotificationListenerService() {

    private lateinit var agentCore: AgentCore

    override fun onCreate() {
        super.onCreate()
        val executor = ActionExecutor(applicationContext)
        val classifier = IntentClassifier()
        val memoryManager = MemoryManager(applicationContext)
        agentCore = AgentCore(applicationContext, classifier, executor, memoryManager)
        Log.d("AetherListener", "Notification listener created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val notification = sbn.notification
        var text = extractTextFromNotification(notification)

        // Fallback to tickerText and package name if main text is empty
        if (text.isBlank()) {
            val ticker = notification.tickerText?.toString() ?: ""
            val pkg = sbn.packageName ?: ""
            text = "$pkg $ticker".trim()
        }

        if (text.isNotBlank()) {
            agentCore.processTrigger(text)
        }
    }

    private fun extractTextFromNotification(notification: Notification): String {
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        return "$title $text".trim()
    }
}
