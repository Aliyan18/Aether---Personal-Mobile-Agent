package com.aether.agent.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aether.agent.utils.PreferencesManager

@Composable
fun MainScreen(prefs: PreferencesManager) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isAgentEnabled by remember { mutableStateOf(prefs.isAgentEnabled()) }
    var isProactiveMode by remember { mutableStateOf(prefs.isProactiveMode()) }
    var isScreenReadingEnabled by remember { mutableStateOf(prefs.isScreenReadingEnabled()) }
    var isAutoClickEnabled by remember { mutableStateOf(prefs.isAutoClickEnabled()) }
    var isLocationMonitoringEnabled by remember { mutableStateOf(prefs.isLocationMonitoringEnabled()) }
    var isScheduledTasksEnabled by remember { mutableStateOf(prefs.isScheduledTasksEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Aether Agent Settings", style = MaterialTheme.typography.headlineMedium)

        SettingToggle(
            label = "Agent Master Switch",
            checked = isAgentEnabled,
            onCheckedChange = {
                isAgentEnabled = it
                prefs.setAgentEnabled(it)
            }
        )

        HorizontalDivider()

        Text("Automation Features", style = MaterialTheme.typography.titleMedium)

        SettingToggle(
            label = "Proactive Mode",
            checked = isProactiveMode,
            onCheckedChange = {
                isProactiveMode = it
                prefs.setProactiveMode(it)
            }
        )

        SettingToggle(
            label = "Screen Reading (Accessibility)",
            checked = isScreenReadingEnabled,
            onCheckedChange = {
                isScreenReadingEnabled = it
                prefs.setScreenReadingEnabled(it)
            }
        )

        SettingToggle(
            label = "Auto-Click Buttons",
            checked = isAutoClickEnabled,
            onCheckedChange = {
                isAutoClickEnabled = it
                prefs.setAutoClickEnabled(it)
            }
        )

        SettingToggle(
            label = "Location Monitoring",
            checked = isLocationMonitoringEnabled,
            onCheckedChange = {
                isLocationMonitoringEnabled = it
                prefs.setLocationMonitoringEnabled(it)
            }
        )

        SettingToggle(
            label = "Scheduled Background Tasks",
            checked = isScheduledTasksEnabled,
            onCheckedChange = {
                isScheduledTasksEnabled = it
                prefs.setScheduledTasksEnabled(it)
            }
        )

        HorizontalDivider()

        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Enable Notification Access")
        }

        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Enable Accessibility Service")
        }
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
