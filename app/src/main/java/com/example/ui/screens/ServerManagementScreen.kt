package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagementScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val servers by viewModel.servers.collectAsState()
  val selectedId by viewModel.selectedServerId.collectAsState()
  val activeTab by viewModel.managementTab.collectAsState()

  val server = servers.find { it.id == selectedId } ?: servers.firstOrNull()

  BackHandler {
    viewModel.navigateBack()
  }

  if (server == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Server not found", style = MaterialTheme.typography.bodyLarge)
    }
    return
  }

  val isOnline = server.status == ServerStatus.ONLINE
  val isOperating = server.status.isOperating()

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("server_management_screen")
  ) {
    // Top App Bar with back navigation
    Surface(
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.testTag("mgmt_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = server.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              EuiStatusBadge(status = server.status)
            }
            Text(
              text = "${server.ipAddress}:${server.port} • ${server.region}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Controls: Start, Stop, Restart
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Start / Stop Button
          Button(
            onClick = { viewModel.toggleServerPower(server.id) },
            enabled = !isOperating,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              contentColor = Color.White
            ),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("mgmt_power_button")
          ) {
            if (isOperating) {
              CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
              )
            } else if (isOnline) {
              Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Stop", style = MaterialTheme.typography.labelLarge)
            } else {
              Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Start", style = MaterialTheme.typography.labelLarge)
            }
          }

          // Restart Button
          OutlinedButton(
            onClick = { viewModel.requestRestart(server.id) },
            enabled = isOnline && !isOperating,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("mgmt_restart_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.RestartAlt,
              contentDescription = null,
              tint = if (isOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Restart",
              style = MaterialTheme.typography.labelLarge,
              color = if (isOnline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
          }

          // Copy Address Action
          OutlinedButton(
            onClick = {
              viewModel.emitFeedback("Copied ${server.ipAddress}:${server.port}")
            },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier
              .height(44.dp)
              .testTag("mgmt_copy_address_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.ContentCopy,
              contentDescription = "Copy IP",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Tab Navigation Bar
      ScrollableTabRow(
        selectedTabIndex = activeTab,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.secondary,
        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline) },
        modifier = Modifier.fillMaxWidth()
      ) {
        val tabs = listOf("Metrics", "Configuration", "Worlds", "Players", "Backups", "Logs")
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = activeTab == index,
            onClick = { viewModel.setManagementTab(index) },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium
              )
            },
            modifier = Modifier.testTag("mgmt_tab_$index")
          )
        }
      }
    }

    // Tab Contents
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (activeTab) {
        0 -> MetricsTabContent(server = server)
        1 -> ConfigurationTabContent(server = server, onSave = { viewModel.saveServerProperties(server.id, it) })
        2 -> WorldsTabContent(server = server)
        3 -> PlayersTabContent(
          server = server,
          onKick = { viewModel.kickPlayer(server.id, it) },
          onToggleOp = { viewModel.togglePlayerOp(server.id, it) }
        )
        4 -> BackupsTabContent(
          server = server,
          onCreateBackup = { viewModel.createBackupNow(server.id) },
          onRestore = { viewModel.emitFeedback("Restoring snapshot '$it'...") }
        )
        5 -> LogsTabContent(
          serverId = server.id,
          viewModel = viewModel
        )
      }
    }
  }
}

@Composable
private fun MetricsTabContent(server: ServerModel) {
  val res = server.resources
  val ramProgress = if (res.ramTotalGb > 0) res.ramUsedGb / res.ramTotalGb else 0f
  val diskProgress = if (res.storageTotalGb > 0) res.storageUsedGb / res.storageTotalGb else 0f

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      EuiSectionHeader(title = "Hardware Telemetry", badge = "GCP Compute Engine")
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        EuiMetricCard(
          label = "CPU UTILIZATION",
          valueText = "${res.cpuPercent}%",
          progress = res.cpuPercent / 100f,
          sublabel = "${res.cpuCores} vCPU AMD EPYC",
          icon = Icons.Outlined.Memory,
          modifier = Modifier.weight(1f)
        )
        EuiMetricCard(
          label = "RAM ALLOCATION",
          valueText = "${res.ramUsedGb} GB",
          progress = ramProgress,
          sublabel = "${res.ramTotalGb.toInt()} GB Total System",
          icon = Icons.Outlined.Storage,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        EuiMetricCard(
          label = "PERSISTENT DISK",
          valueText = "${res.storageUsedGb} GB",
          progress = diskProgress,
          sublabel = "${res.storageTotalGb.toInt()} GB Balanced SSD",
          icon = Icons.Outlined.Folder,
          modifier = Modifier.weight(1f)
        )
        EuiMetricCard(
          label = "NETWORK TRAFFIC",
          valueText = "${res.networkInMb} MB/s",
          progress = (res.networkInMb / 25f).coerceIn(0f, 1f),
          sublabel = "${res.networkOutMb} MB/s Outbound",
          icon = Icons.Outlined.Speed,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      EuiCard {
        Text("SYSTEM SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        SpecRow("GCP Instance ID", server.gcpInstanceId)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SpecRow("Machine Type", server.machineType)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SpecRow("Availability SLA", "${server.resources.slaPercent}%")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SpecRow("Uptime Counter", server.resources.uptimeString)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))
        SpecRow("RakNet Latency Buffer", "16 ms")
      }
    }
  }
}

@Composable
private fun SpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun ConfigurationTabContent(
  server: ServerModel,
  onSave: (ServerProperties) -> Unit
) {
  var motd by remember(server.properties) { mutableStateOf(server.properties.serverName) }
  var port by remember(server.properties) { mutableStateOf(server.properties.serverPort.toString()) }
  var maxPlayers by remember(server.properties) { mutableStateOf(server.properties.maxPlayers.toString()) }
  var viewDistance by remember(server.properties) { mutableStateOf(server.properties.viewDistance.toString()) }
  var pvp by remember(server.properties) { mutableStateOf(server.properties.pvp) }
  var allowCheats by remember(server.properties) { mutableStateOf(server.properties.allowCheats) }
  var whitelist by remember(server.properties) { mutableStateOf(server.properties.whiteListEnabled) }
  var gamemode by remember(server.properties) { mutableStateOf(server.properties.gamemode) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      EuiSectionHeader(title = "server.properties", badge = "Bedrock Configuration")
    }

    item {
      EuiCard {
        OutlinedTextField(
          value = motd,
          onValueChange = { motd = it },
          label = { Text("Server Name / MOTD") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("UDP Port") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          )
          OutlinedTextField(
            value = maxPlayers,
            onValueChange = { maxPlayers = it },
            label = { Text("Max Players") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = viewDistance,
          onValueChange = { viewDistance = it },
          label = { Text("View Distance (Chunks)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(12.dp))

        // Toggle rows
        ConfigSwitchRow("Player vs Player (PvP)", "Allow combat between players", pvp) { pvp = it }
        ConfigSwitchRow("Allow Cheats", "Permit slash commands like /gamemode", allowCheats) { allowCheats = it }
        ConfigSwitchRow("Enforce Whitelist", "Restrict server entry to approved accounts", whitelist) { whitelist = it }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            onSave(
              server.properties.copy(
                serverName = motd,
                serverPort = port.toIntOrNull() ?: 19132,
                maxPlayers = maxPlayers.toIntOrNull() ?: 50,
                viewDistance = viewDistance.toIntOrNull() ?: 16,
                pvp = pvp,
                allowCheats = allowCheats,
                whiteListEnabled = whitelist,
                gamemode = gamemode
              )
            )
          },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_server_properties_button")
        ) {
          Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save Configuration", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}

@Composable
private fun ConfigSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun WorldsTabContent(server: ServerModel) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      EuiSectionHeader(
        title = "Managed Worlds",
        badge = "${server.worlds.size} Worlds Mounted",
        actionText = "Add World",
        onActionClick = { /* simulation */ }
      )
    }

    items(server.worlds) { world ->
      EuiCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Public,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp)
            )
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = world.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                if (world.isDefault) {
                  Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("DEFAULT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                  }
                }
              }
              Text("Seed: ${world.seed} • Type: ${world.levelType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Text("${world.sizeMb} MB", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
      }
    }
  }
}

@Composable
private fun PlayersTabContent(
  server: ServerModel,
  onKick: (String) -> Unit,
  onToggleOp: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      EuiSectionHeader(
        title = "Connected Players",
        badge = "${server.players.size} Online",
        actionText = "Whitelist Manager",
        onActionClick = {}
      )
    }

    if (server.players.isEmpty()) {
      item {
        EuiCard {
          Text("No players currently connected to this realm.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      items(server.players) { player ->
        EuiCard {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = null,
                tint = if (player.isOp) LocalEuiStatusColors.current.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
              )
              Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(text = player.username, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                  if (player.isOp) {
                    Surface(shape = RoundedCornerShape(4.dp), color = LocalEuiStatusColors.current.warningContainer) {
                      Text("OP", style = MaterialTheme.typography.labelSmall, color = LocalEuiStatusColors.current.onWarningContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                  }
                }
                Text("Ping: ${player.pingMs}ms • IP: ${player.ipAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = { onToggleOp(player.xuid) },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.defaultMinSize(minHeight = 32.dp)
              ) {
                Text(if (player.isOp) "De-Op" else "Op", style = MaterialTheme.typography.labelSmall)
              }

              OutlinedButton(
                onClick = { onKick(player.xuid) },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.defaultMinSize(minHeight = 32.dp)
              ) {
                Text("Kick", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BackupsTabContent(
  server: ServerModel,
  onCreateBackup: () -> Unit,
  onRestore: (String) -> Unit
) {
  val statusColors = LocalEuiStatusColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Automated Backups", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
          Text("Retained for 7 days in Cloud Storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
          onClick = onCreateBackup,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.height(40.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Create Snapshot", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    items(server.backups) { snapshot ->
      EuiCard {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Inventory2,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(22.dp)
            )
            Column {
              Text(text = snapshot.timestamp, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
              Text(text = "${snapshot.type} • ${snapshot.sizeMb} MB • Status: ${snapshot.status}", style = MaterialTheme.typography.bodySmall, color = statusColors.success)
            }
          }

          OutlinedButton(
            onClick = { onRestore(snapshot.id) },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.defaultMinSize(minHeight = 34.dp)
          ) {
            Text("Restore", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
private fun LogsTabContent(
  serverId: String,
  viewModel: PocketCoreViewModel
) {
  // Directly embeds the clean enterprise console viewer
  ConsoleScreen(
    viewModel = viewModel,
    targetServerId = serverId
  )
}
