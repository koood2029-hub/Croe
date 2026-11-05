package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.ServerModel
import com.example.data.model.ServerStatus
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiStatusBadge
import com.example.ui.viewmodel.PocketCoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val servers by viewModel.servers.collectAsState()
  val searchQuery by viewModel.serverSearchQuery.collectAsState()
  val filterStatus by viewModel.serverFilterStatus.collectAsState()

  val filteredServers = remember(servers, searchQuery, filterStatus) {
    servers.filter { server ->
      val matchesSearch = server.name.contains(searchQuery, ignoreCase = true) ||
          server.ipAddress.contains(searchQuery, ignoreCase = true) ||
          server.region.contains(searchQuery, ignoreCase = true)
      val matchesStatus = when (filterStatus) {
        null -> true
        ServerStatus.ONLINE -> server.status == ServerStatus.ONLINE
        ServerStatus.OFFLINE -> server.status == ServerStatus.OFFLINE
        else -> server.status == filterStatus
      }
      matchesSearch && matchesStatus
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("my_servers_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Row with Title & Create Server button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "My Servers",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${servers.size} Managed Bedrock Instances",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = { viewModel.startCloudWizard() },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
          ),
          modifier = Modifier
            .height(44.dp)
            .testTag("create_server_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            "Create Server",
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }

    // Search input
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setServerSearch(it) },
        placeholder = {
          Text("Search servers by name, IP, or region...", style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedBorderColor = MaterialTheme.colorScheme.secondary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("servers_search_field")
      )
    }

    // Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = filterStatus == null,
          onClick = { viewModel.setServerFilter(null) },
          label = { Text("All (${servers.size})", style = MaterialTheme.typography.labelMedium) },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.testTag("filter_all_servers")
        )
        FilterChip(
          selected = filterStatus == ServerStatus.ONLINE,
          onClick = { viewModel.setServerFilter(ServerStatus.ONLINE) },
          label = {
            Text("Running (${servers.count { it.status == ServerStatus.ONLINE }})", style = MaterialTheme.typography.labelMedium)
          },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.testTag("filter_running_servers")
        )
        FilterChip(
          selected = filterStatus == ServerStatus.OFFLINE,
          onClick = { viewModel.setServerFilter(ServerStatus.OFFLINE) },
          label = {
            Text("Stopped (${servers.count { it.status == ServerStatus.OFFLINE }})", style = MaterialTheme.typography.labelMedium)
          },
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.testTag("filter_stopped_servers")
        )
      }
    }

    // Server Cards List
    if (filteredServers.isEmpty()) {
      item {
        EmptyServersState(searchQuery = searchQuery)
      }
    } else {
      items(filteredServers, key = { it.id }) { server ->
        ServerCardItem(
          server = server,
          onManageClick = { viewModel.selectAndManageServer(server.id) },
          onTogglePower = { viewModel.toggleServerPower(server.id) }
        )
      }
    }
  }
}

@Composable
private fun ServerCardItem(
  server: ServerModel,
  onManageClick: () -> Unit,
  onTogglePower: () -> Unit
) {
  val isOnline = server.status == ServerStatus.ONLINE
  val isOperating = server.status.isOperating()

  EuiCard(onClick = onManageClick) {
    // Top Row: Name, Status badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = server.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${server.ipAddress}:${server.port}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.secondary,
          fontWeight = FontWeight.Medium
        )
      }
      EuiStatusBadge(status = server.status)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Middle Meta Row: Bedrock version, Machine, Region, Players
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "VERSION",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = server.bedrockVersion,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Column {
        Text(
          text = "REGION",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = server.region.substringBefore(" "),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "PLAYERS",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "${server.currentPlayers} / ${server.maxPlayers}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Action Row: Manage Server + Quick Power
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = onManageClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .testTag("manage_server_${server.id}")
      ) {
        Icon(
          imageVector = Icons.Outlined.DashboardCustomize,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "Manage Server",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      IconButton(
        onClick = onTogglePower,
        enabled = !isOperating,
        modifier = Modifier
          .size(44.dp)
          .testTag("power_server_${server.id}")
      ) {
        if (isOperating) {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.secondary
          )
        } else if (isOnline) {
          Icon(
            imageVector = Icons.Outlined.PowerSettingsNew,
            contentDescription = "Stop Server",
            tint = MaterialTheme.colorScheme.error
          )
        } else {
          Icon(
            imageVector = Icons.Outlined.PlayCircleOutline,
            contentDescription = "Start Server",
            tint = MaterialTheme.colorScheme.secondary
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyServersState(searchQuery: String) {
  EuiCard {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.Dns,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(36.dp)
      )
      Text(
        text = if (searchQuery.isNotEmpty()) "No servers matching '$searchQuery'" else "No server instances deployed",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Deploy a new PocketCore Bedrock instance to get started.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
