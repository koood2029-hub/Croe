package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogEntry
import com.example.data.model.LogLevel
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel
import kotlinx.coroutines.launch

@Composable
fun ConsoleScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier,
  targetServerId: String? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val selectedId by viewModel.selectedServerId.collectAsState()
  val activeServerId = targetServerId ?: selectedId

  val allLogs by viewModel.consoleLogs.collectAsState()
  val logsForServer = allLogs[activeServerId] ?: emptyList()

  val filterLevel by viewModel.logFilterLevel.collectAsState()
  val searchQuery by viewModel.logSearchQuery.collectAsState()

  var commandInput by remember { mutableStateOf("") }

  val filteredLogs = remember(logsForServer, filterLevel, searchQuery) {
    logsForServer.filter { entry ->
      val matchesLevel = filterLevel == LogLevel.ALL || entry.level == filterLevel
      val matchesSearch = searchQuery.isEmpty() ||
          entry.message.contains(searchQuery, ignoreCase = true) ||
          entry.thread.contains(searchQuery, ignoreCase = true)
      matchesLevel && matchesSearch
    }
  }

  // Auto-scroll to latest log on update
  LaunchedEffect(filteredLogs.size) {
    if (filteredLogs.isNotEmpty()) {
      listState.animateScrollToItem(filteredLogs.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("console_screen")
  ) {
    // Toolbar: Filter chips + Search + Copy logs action
    Surface(
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Dns,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Cloud Log Stream",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val fullText = filteredLogs.joinToString("\n") { "[${it.timestamp}] [${it.level.name}] ${it.message}" }
              clipboard.setPrimaryClip(ClipData.newPlainText("Server Logs", fullText))
              viewModel.emitFeedback("Console logs copied to clipboard")
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.ContentCopy,
              contentDescription = "Copy logs",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search in logs
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setLogSearch(it) },
          placeholder = { Text("Filter logs...", style = MaterialTheme.typography.bodySmall) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
          },
          singleLine = true,
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Log level filters
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val levels = listOf(LogLevel.ALL, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.COMMAND)
          items(levels) { lvl ->
            FilterChip(
              selected = filterLevel == lvl,
              onClick = { viewModel.setLogFilter(lvl) },
              label = { Text(lvl.name, style = MaterialTheme.typography.labelSmall) },
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.height(30.dp)
            )
          }
        }
      }
    }

    // Main Log Stream Container (Clean enterprise reader, clear monospace, NO hacker green)
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (filteredLogs.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No log records matching filter",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredLogs, key = { it.id }) { log ->
            LogLineItem(log = log)
          }
        }
      }
    }

    // Quick Command Suggestion Chips
    Surface(
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val suggestions = listOf("/status", "/help", "/say Maintenance in 10m", "/whitelist", "/time set day")
          items(suggestions) { cmd ->
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              onClick = { commandInput = cmd }
            ) {
              Text(
                text = cmd,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Command Input Field
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = commandInput,
            onValueChange = { commandInput = it },
            placeholder = { Text("Enter server command (e.g. /say Hello)...", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("console_command_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedBorderColor = MaterialTheme.colorScheme.secondary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )

          Button(
            onClick = {
              if (commandInput.isNotBlank()) {
                viewModel.executeConsoleCommand(activeServerId, commandInput)
                commandInput = ""
              }
            },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
              .height(48.dp)
              .testTag("console_command_send_button")
          ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun LogLineItem(log: LogEntry) {
  val statusColors = LocalEuiStatusColors.current

  val (tagColor, tagBg) = when (log.level) {
    LogLevel.INFO -> Pair(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
    LogLevel.WARN -> Pair(statusColors.onWarningContainer, statusColors.warningContainer)
    LogLevel.ERROR -> Pair(statusColors.onErrorContainer, statusColors.errorContainer)
    LogLevel.COMMAND -> Pair(MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer)
    else -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Timestamp
    Text(
      text = log.timestamp,
      style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )

    // Severity indicator badge
    Surface(
      shape = RoundedCornerShape(3.dp),
      color = tagBg,
      modifier = Modifier.padding(top = 1.dp)
    ) {
      Text(
        text = log.level.name,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
        color = tagColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
      )
    }

    // Message
    Text(
      text = log.message,
      style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )
  }
}
