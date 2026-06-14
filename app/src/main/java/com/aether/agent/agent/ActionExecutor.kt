package com.aether.agent.agent

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import com.aether.agent.utils.PreferencesManager

class ActionExecutor(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = PreferencesManager(context)

    fun execute(action: String, parameters: Map<String, String> = emptyMap()): Boolean {
        if (!prefs.isAgentEnabled()) return false

        return when (action) {
            "mute_phone" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
                true
            }
            "unmute_phone" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVol, 0)
                } else {
                    audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                }
                true
            }
            "send_sms" -> {
                val to = parameters["to"] ?: return false
                val message = parameters["message"] ?: return false
                sendSms(to, message)
            }
            "create_calendar_event" -> {
                val title = parameters["title"] ?: return false
                val time = parameters["time"] ?: return false
                createCalendarEvent(title, time)
            }
            else -> false
        }
    }

    private fun sendSms(to: String, message: String): Boolean {
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(to, null, message, null, null)
            true
        } catch (e: Exception) {
            Log.e("ActionExecutor", "SMS failed: ${e.message}")
            false
        }
    }

    private fun createCalendarEvent(title: String, time: String): Boolean {
        // Simplified - just logs for now
        Log.d("ActionExecutor", "Would create calendar: $title at $time")
        // You can implement actual CalendarContract insertion later
        return true
    }
}
