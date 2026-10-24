package com.aether.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aether.agent.agent.ActionExecutor
import com.aether.agent.agent.AgentCore
import com.aether.agent.agent.IntentClassifier
import com.aether.agent.memory.MemoryManager
import com.aether.agent.services.AetherForegroundService
import com.aether.agent.triggers.LocationTrigger
import com.aether.agent.triggers.TimeTrigger
import com.aether.agent.ui.MainScreen
import com.aether.agent.ui.PrivacyScreen
import com.aether.agent.utils.PreferencesManager

class MainActivity : ComponentActivity() {

    private lateinit var locationTrigger: LocationTrigger
    private lateinit var timeTrigger: TimeTrigger
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize crash reporting
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        
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
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            NavigationBarItem(
                                selected = currentRoute == "main",
                                onClick = { navController.navigate("main") },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Main") },
                                label = { Text("Main") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "history",
                                onClick = { navController.navigate("history") },
                                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                label = { Text("History") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") }
                            )
                            // Add to your existing bottom navigation items
                            NavigationBarItem(
                                selected = currentRoute == "privacy",
                                onClick = { navController.navigate("privacy") },
                                icon = { Icon(Icons.Default.Shield, contentDescription = "Privacy") },
                                label = { Text("Privacy") }
                            )
                        }
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") {
                                Text("Main Screen Content") // Placeholder
                            }
                            composable("history") {
                                Text("History Screen Content") // Placeholder
                            }
                            composable("settings") {
                                MainScreen(prefs)
                            }
                            // Add composable (next to your existing main, settings, history)
                            composable("privacy") {
                                PrivacyScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
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
