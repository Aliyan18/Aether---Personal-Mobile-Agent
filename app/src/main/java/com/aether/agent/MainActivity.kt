package com.aether.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.agent.AgentCore
import com.aether.agent.agent.IntentClassifier
import com.aether.agent.memory.MemoryManager
import com.aether.agent.services.AetherForegroundService
import com.aether.agent.triggers.LocationTrigger
import com.aether.agent.triggers.TimeTrigger
import com.aether.agent.ui.MainScreen
import com.aether.agent.utils.PreferencesManager

class MainActivity : ComponentActivity() {

    private lateinit var locationTrigger: LocationTrigger
    private lateinit var timeTrigger: TimeTrigger
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = PreferencesManager(this)

        // Request runtime permissions
        requestPermissions()

        // Start foreground service
        val serviceIntent = Intent(this, AetherForegroundService::class.java)
        startForegroundService(serviceIntent)

        // Initialize triggers
        val agentCore = AgentCore(
            this,
            IntentClassifier(),
            ActionExecutor(this),
            MemoryManager(this)
        )

        locationTrigger = LocationTrigger(this, agentCore)
        timeTrigger = TimeTrigger(this, agentCore)

        // Start monitoring
        if (hasLocationPermission()) {
            locationTrigger.startMonitoring()
        }

        // Schedule daily check at 8am
        timeTrigger.scheduleDailyCheck(8, 0)

        // Schedule recurring check every 30 minutes
        timeTrigger.scheduleRecurringCheck(30)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(prefs)
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
