package com.aether.agent.utils

import android.content.Context

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("aether_prefs", Context.MODE_PRIVATE)

    fun isAgentEnabled(): Boolean = prefs.getBoolean("agent_enabled", true)
    fun setAgentEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("agent_enabled", enabled).apply()
    }

    /**
     * Proactive mode – agent can act without being asked
     * Default: true (recommended, but user can disable for privacy)
     */
    fun isProactiveMode(): Boolean = prefs.getBoolean("proactive_mode", true)

    fun setProactiveMode(enabled: Boolean) {
        prefs.edit().putBoolean("proactive_mode", enabled).apply()
    }

    /**
     * Auto-execute confidence threshold (0.0 to 1.0)
     * Actions above this confidence execute without asking
     * Default: 0.85 (85% confident)
     */
    fun getAutoExecuteThreshold(): Float = prefs.getFloat("auto_execute_threshold", 0.85f)

    fun setAutoExecuteThreshold(threshold: Float) {
        prefs.edit().putFloat("auto_execute_threshold", threshold).apply()
    }

    /**
     * Whether to read screen content via AccessibilityService
     * Default: true (required for proactive features)
     */
    fun isScreenReadingEnabled(): Boolean = prefs.getBoolean("screen_reading_enabled", true)

    fun setScreenReadingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("screen_reading_enabled", enabled).apply()
    }

    /**
     * Whether to auto-click buttons (e.g., "OK", "Accept")
     * Default: true (helpful automation)
     */
    fun isAutoClickEnabled(): Boolean = prefs.getBoolean("auto_click_enabled", true)

    fun setAutoClickEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_click_enabled", enabled).apply()
    }

    /**
     * Location monitoring enabled
     * Default: false (user must opt-in for privacy)
     */
    fun isLocationMonitoringEnabled(): Boolean = prefs.getBoolean("location_monitoring_enabled", false)

    fun setLocationMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("location_monitoring_enabled", enabled).apply()
    }

    /**
     * Scheduled tasks enabled (daily check, recurring scans)
     * Default: true
     */
    fun isScheduledTasksEnabled(): Boolean = prefs.getBoolean("scheduled_tasks_enabled", true)

    fun setScheduledTasksEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("scheduled_tasks_enabled", enabled).apply()
    }
}
