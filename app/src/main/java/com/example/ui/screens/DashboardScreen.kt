package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerActivity
import com.example.data.model.ServerModel
import com.example.data.model.ServerStatus
import com.example.ui.components.*
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun DashboardScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val servers by viewModel.servers.collectAsState()
  val selectedId by viewModel.selectedServerId.collectAsState()
  val notifications by viewModel.notifications.collectAsState()

  val primaryServer = servers.find { it.id == selectedId } ?: servers.firstOrNull()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar / Welcome Card
    item {
      DashboardHeader(
        onNotificationClick = { viewModel.toggleNotificationsSheet(true) },
        onSettingsClick = { viewModel.navigateTo(Screen.SETTINGS) }
      )
    }

    // Cloud Connection Status Banner
    item {
      CloudConnectionCard(
        region = primaryServer?.region ?: "us-central1 (Iowa)",
        sla = primaryServer?.resources?.slaPercent ?: 99.98f,
        onDeployClick = { viewModel.startCloudWizard() }
      )
    }

    // Primary Server Card
    if (primaryServer != null) {
      item {
        PrimaryServerCard(
          server = primaryServer,
          onTogglePower = { viewModel.toggleServerPower(primaryServer.id) },
          onManageClick = { viewModel.selectAndManageServer(primaryServer.id) },
          onCopyAddress = { viewModel.emitFeedback("Server address copied to clipboard") }
        )
      }

      // Resource Monitoring Grid
      item {
        EuiSectionHeader(
          title = "Resource Telemetry",
          badge = primaryServer.machineType.substringBefore(" ")
        )
      }

      item {
        ResourceMonitoringGrid(server = primaryServer)
      }

      // Backup Status Section
      item {
        BackupStatusCard(
          server = primaryServer,
          onCreateBackup = { viewModel.createBackupNow(primaryServer.id) }
        )
      }

      // Recent Activity
      item {
        EuiSectionHeader(
          title = "Recent Server Activity",
          actionText = "Audit Log",
          onActionClick = { viewModel.selectAndManageServer(primaryServer.id, tab = 4) }
        )
      }

      items(primaryServer.activities.take(4)) { activity ->
        ActivityItemRow(activity = activity)
      }
    }
  }
}

@Composable
private fun DashboardHeader(
  onNotificationClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Enterprise logo mark
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Outlined.CloudQueue,
            contentDescription = "PocketCore Logo",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      }
      Column {
        Text(
          text = "PocketCore Cloud",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Enterprise Bedrock Cluster",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onNotificationClick,
        modifier = Modifier
          .size(44.dp)
          .testTag("dashboard_notifications_button")
      ) {
        BadgedBox(
          badge = {
            Badge(
              containerColor = MaterialTheme.colorScheme.secondary,
              contentColor = Color.White
            ) {
              Text("4", style = MaterialTheme.typography.labelSmall)
            }
          }
        ) {
          Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
      ) {
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier.testTag("dashboard_profile_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Account Profile",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CloudConnectionCard(
  region: String,
  sla: Float,
  onDeployClick: () -> Unit
) {
  val statusColors = LocalEuiStatusColors.current
  EuiCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(statusColors.success)
        )
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Connected to Google Cloud",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "• SLA ${sla}%",
              style = MaterialTheme.typography.labelSmall,
              color = statusColors.success,
              fontWeight = FontWeight.SemiBold
            )
          }
          Text(
            text = "Infrastructure: $region • Project: pocketcore-prod-39440",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      OutlinedButton(
        onClick = onDeployClick,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier
          .defaultMinSize(minHeight = 36.dp)
          .testTag("deploy_new_server_button")
      ) {
        Icon(
          imageVector = Icons.Outlined.Add,
          contentDescription = null,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Deploy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun PrimaryServerCard(
  server: ServerModel,
  onTogglePower: () -> Unit,
  onManageClick: () -> Unit,
  onCopyAddress: () -> Unit
) {
  val isOperating = server.status.isOperating()
  val isOnline = server.status == ServerStatus.ONLINE

  EuiCard(
    border = BorderStroke(1.5.dp, if (isOnline) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline)
  ) {
    // Header row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "PRIMARY SERVER",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = server.name,
          style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${server.bedrockVersion} • ${server.pocketMineVersion}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      EuiStatusBadge(status = server.status)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Server Address Box with Copy
    EuiCopyField(
      label = "SERVER ADDRESS (BEDROCK UDP)",
      value = "${server.ipAddress}:${server.port}",
      onCopied = onCopyAddress
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Key Stats Row: Players & Uptime
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.People,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
        Column {
          Text(
            text = "Online Players",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${server.currentPlayers} / ${server.maxPlayers} players",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Timer,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Server Uptime",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = server.resources.uptimeString,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Large START / STOP Button + Manage Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Primary Action Button
      Button(
        onClick = onTogglePower,
        enabled = !isOperating,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ),
        modifier = Modifier
          .weight(1.4f)
          .height(48.dp)
          .testTag("primary_server_power_button")
      ) {
        if (isOperating) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("PROCESSING...", style = MaterialTheme.typography.labelLarge)
        } else if (isOnline) {
          Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("STOP SERVER", style = MaterialTheme.typography.labelLarge)
        } else {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("START SERVER", style = MaterialTheme.typography.labelLarge)
        }
      }

      // Manage Server Details Button
      OutlinedButton(
        onClick = onManageClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("manage_primary_server_button")
      ) {
        Icon(
          imageVector = Icons.Outlined.Settings,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "Manage",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun ResourceMonitoringGrid(server: ServerModel) {
  val res = server.resources
  val ramProgress = if (res.ramTotalGb > 0) res.ramUsedGb / res.ramTotalGb else 0f
  val diskProgress = if (res.storageTotalGb > 0) res.storageUsedGb / res.storageTotalGb else 0f
  val cpuProgress = res.cpuPercent / 100f

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      EuiMetricCard(
        label = "CPU USAGE",
        valueText = "${res.cpuPercent}%",
        progress = cpuProgress,
        sublabel = "${res.cpuCores} vCPUs allocated",
        icon = Icons.Outlined.Memory,
        modifier = Modifier.weight(1f),
        progressColor = if (res.cpuPercent > 80f) LocalEuiStatusColors.current.warning else MaterialTheme.colorScheme.secondary
      )

      EuiMetricCard(
        label = "MEMORY (RAM)",
        valueText = "${res.ramUsedGb} GB",
        progress = ramProgress,
        sublabel = "of ${res.ramTotalGb.toInt()} GB limit",
        icon = Icons.Outlined.Storage,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      EuiMetricCard(
        label = "STORAGE (NVME)",
        valueText = "${res.storageUsedGb} GB",
        progress = diskProgress,
        sublabel = "of ${res.storageTotalGb.toInt()} GB capacity",
        icon = Icons.Outlined.Folder,
        modifier = Modifier.weight(1f)
      )

      EuiMetricCard(
        label = "NETWORK I/O",
        valueText = "${res.networkInMb} MB/s",
        progress = (res.networkInMb / 20f).coerceIn(0f, 1f),
        sublabel = "↑ ${res.networkOutMb} MB/s egress",
        icon = Icons.Outlined.Speed,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun BackupStatusCard(
  server: ServerModel,
  onCreateBackup: () -> Unit
) {
  val statusColors = LocalEuiStatusColors.current
  val latest = server.backups.firstOrNull()

  EuiCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusColors.successContainer,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Outlined.CloudDone,
              contentDescription = null,
              tint = statusColors.onSuccessContainer,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Column {
          Text(
            text = "Cloud Backup Snapshot",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (latest != null) "Last: ${latest.timestamp} (${latest.sizeMb} MB) • Healthy" else "No snapshots recorded",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      OutlinedButton(
        onClick = onCreateBackup,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .defaultMinSize(minHeight = 38.dp)
          .testTag("create_backup_button")
      ) {
        Icon(
          imageVector = Icons.Outlined.Backup,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          "Snapshot",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun ActivityItemRow(activity: ServerActivity) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val (icon, tint) = when (activity.category) {
          "Player" -> Pair(Icons.Outlined.Person, MaterialTheme.colorScheme.secondary)
          "Backup" -> Pair(Icons.Outlined.CloudDone, LocalEuiStatusColors.current.success)
          "Security" -> Pair(Icons.Outlined.Security, LocalEuiStatusColors.current.warning)
          else -> Pair(Icons.Outlined.SettingsSuggest, MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(18.dp)
        )

        Column {
          Text(
            text = activity.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "by ${activity.actor} • ${activity.timestamp}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
