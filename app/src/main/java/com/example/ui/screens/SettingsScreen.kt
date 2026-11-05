package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiSectionHeader
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel

@Composable
fun SettingsScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val statusColors = LocalEuiStatusColors.current

  // Settings states
  var restartOnCrash by remember { mutableStateOf(true) }
  var notifyOnCrash by remember { mutableStateOf(true) }
  var notifyOnHighMem by remember { mutableStateOf(true) }
  var notifyOnJoin by remember { mutableStateOf(false) }
  var rconWhitelist by remember { mutableStateOf(true) }
  var auditLogging by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text("Settings & Preferences", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Manage enterprise configurations and cloud infrastructure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    // 1. Account Section
    item {
      EuiSectionHeader(title = "Account", badge = "Enterprise Admin")
    }

    item {
      EuiCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("yesboy200005@gmail.com", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Organization Admin • Google Cloud Identity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(10.dp))
        SettingItemText("Cloud Instance Quota", "3 of 10 Bedrock instances active")
        SettingItemText("Billing Profile", "Google Cloud Monthly Invoicing (Active)")
      }
    }

    // 2. Google Cloud Connection
    item {
      EuiSectionHeader(title = "Google Cloud Connection", badge = "us-central1")
    }

    item {
      EuiCard {
        SettingItemText("Active Project ID", "pocketcore-prod-39440")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemText("Service Account Status", "Authenticated & Provisioned")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemText("Compute Engine API", "v1 Enabled")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemText("Cloud Storage Bucket", "gs://pocketcore-backups-vault")
      }
    }

    // 3. Server Defaults
    item {
      EuiSectionHeader(title = "Server Defaults")
    }

    item {
      EuiCard {
        SettingItemText("Default Bedrock Version", "v1.21.50 (PocketMine-MP 5.14)")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemText("Automated Backup Schedule", "Every 6 hours (Snapshot retain: 7d)")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemSwitch("Auto-Restart on Crash", "Automatically cycle container if unhandled fault occurs", restartOnCrash) {
          restartOnCrash = it
          viewModel.emitFeedback("Server default updated")
        }
      }
    }

    // 4. Notifications
    item {
      EuiSectionHeader(title = "Notifications & Alerts")
    }

    item {
      EuiCard {
        SettingItemSwitch("Server Crash Alerts", "Immediate priority push notifications", notifyOnCrash) { notifyOnCrash = it }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
        SettingItemSwitch("High Memory Alert (>85%)", "Warn when RAM usage exceeds threshold", notifyOnHighMem) { notifyOnHighMem = it }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
        SettingItemSwitch("Player Join/Leave Notices", "Audit log real-time player telemetry", notifyOnJoin) { notifyOnJoin = it }
      }
    }

    // 5. Appearance
    item {
      EuiSectionHeader(title = "Appearance & Theme")
    }

    item {
      EuiCard {
        Text("THEME MODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = isDarkTheme == null,
            onClick = { viewModel.setDarkModePreference(null) },
            label = { Text("System", style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = isDarkTheme == false,
            onClick = { viewModel.setDarkModePreference(false) },
            label = { Text("Light", style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = isDarkTheme == true,
            onClick = { viewModel.setDarkModePreference(true) },
            label = { Text("Dark", style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 6. Storage
    item {
      EuiSectionHeader(title = "Local Storage & Cache")
    }

    item {
      EuiCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Local Plugin & Cache Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("34.2 MB occupied on device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          OutlinedButton(
            onClick = { viewModel.emitFeedback("Local cache cleared (34.2 MB freed)") },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
          ) {
            Text("Clear Cache", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }

    // 7. Security
    item {
      EuiSectionHeader(title = "Security & Governance")
    }

    item {
      EuiCard {
        SettingItemText("Two-Factor Authentication", "Enforced via Google Workspace")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SettingItemSwitch("RCON IP Whitelist", "Block unauthorized remote console connections", rconWhitelist) { rconWhitelist = it }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
        SettingItemSwitch("Audit Trail Logging", "Retain administrative change logs for 30 days", auditLogging) { auditLogging = it }
      }
    }

    // 8. About
    item {
      EuiSectionHeader(title = "About")
    }

    item {
      EuiCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("PocketCore Cloud Enterprise", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Version 2.4.0-enterprise • Build 2026.11", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("STABLE", style = MaterialTheme.typography.labelSmall, color = statusColors.success, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          "Engineered for high-availability Minecraft: Bedrock Edition clusters powered by PocketMine-MP and Google Cloud Platform.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun SettingItemText(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun SettingItemSwitch(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
