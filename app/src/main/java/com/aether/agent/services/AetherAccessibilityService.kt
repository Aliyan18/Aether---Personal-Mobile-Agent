package com.aether.agent.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.agent.AgentCore
import com.aether.agent.agent.IntentClassifier
import com.aether.agent.memory.EpisodeEntity
import com.aether.agent.memory.MemoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AetherAccessibilityService : AccessibilityService() {

    private lateinit var agentCore: AgentCore
    private lateinit var memoryManager: MemoryManager
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastProcessedText = ""
    private var lastProcessedTime = 0L
    private val debounceMs = 2000L // Don't process same text within 2 seconds

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Configure accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100 // milliseconds
        }
        this.serviceInfo = info

        // Initialize agent
        val executor = ActionExecutor(applicationContext)
        val classifier = IntentClassifier()
        memoryManager = MemoryManager(applicationContext)
        agentCore = AgentCore(applicationContext, classifier, executor, memoryManager)

        Log.d("AetherAccessibility", "Service connected and configured")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Don't process our own app's events (avoid loops)
        if (event.packageName?.toString() == "com.aether.agent") return

        // Extract text from the screen
        val root = rootInActiveWindow ?: return
        val screenText = extractAllText(root)

        if (screenText.isNotBlank()) {
            processScreenContent(screenText, event.packageName?.toString())
        }

        // Also check if there's a clickable button we can auto-press
        findAndPressSmartButtons(root)
    }

    private fun extractAllText(node: AccessibilityNodeInfo): String {
        val textBuilder = StringBuilder()

        // Get text from current node
        node.text?.toString()?.let {
            if (it.isNotBlank()) textBuilder.append(it).append(" ")
        }

        // Recursively get text from all children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                textBuilder.append(extractAllText(child))
            }
        }

        return textBuilder.toString().trim()
    }

    private fun processScreenContent(text: String, packageName: String?) {
        val now = System.currentTimeMillis()

        // Debounce: don't process same text repeatedly
        if (text == lastProcessedText && (now - lastProcessedTime) < debounceMs) {
            return
        }

        lastProcessedText = text
        lastProcessedTime = now

        Log.d("AetherAccessibility", "Screen text: ${text.take(200)}...")
        Log.d("AetherAccessibility", "Package: $packageName")

        // Send to agent for processing
        scope.launch {
            val fullTrigger = "$packageName: $text"
            agentCore.processTrigger(fullTrigger, isProactive = true)

            // Store in memory
            val episode = EpisodeEntity(
                triggerText = fullTrigger.take(500),
                actionTaken = "screen_observation",
                outcome = "observed",
                appPackage = packageName,
            )
            memoryManager.remember(episode)
        }
    }

    private fun findAndPressSmartButtons(root: AccessibilityNodeInfo) {
        // Look for common positive action buttons
        val buttonTexts = listOf("Accept", "Allow", "OK", "Yes", "Confirm", "Join")

        for (text in buttonTexts) {
            val buttons = root.findAccessibilityNodeInfosByText(text)
            for (button in buttons) {
                if (button.isClickable) {
                    Log.d("AetherAccessibility", "Auto-pressing button: $text")
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    // Store this auto-action in memory
                    scope.launch {
                        val episode = EpisodeEntity(
                            triggerText = "Auto-pressed $text button",
                            actionTaken = "auto_click",
                            outcome = "success",
                            appPackage = root.packageName?.toString(),
                        )
                        memoryManager.remember(episode)
                    }
                    return // Only press one button per event
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AetherAccessibility", "Service interrupted")
    }
}
