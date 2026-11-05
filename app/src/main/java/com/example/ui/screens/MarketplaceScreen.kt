package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PluginCategory
import com.example.data.model.PluginInfo
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiSectionHeader
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel

@Composable
fun MarketplaceScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val plugins by viewModel.plugins.collectAsState()
  val selectedCategory by viewModel.marketplaceCategory.collectAsState()
  val searchQuery by viewModel.marketplaceSearch.collectAsState()

  val filteredPlugins = remember(plugins, selectedCategory, searchQuery) {
    plugins.filter { plugin ->
      val matchesCat = selectedCategory == PluginCategory.ALL || plugin.category == selectedCategory
      val matchesSearch = plugin.name.contains(searchQuery, ignoreCase = true) ||
          plugin.description.contains(searchQuery, ignoreCase = true) ||
          plugin.author.contains(searchQuery, ignoreCase = true)
      matchesCat && matchesSearch
    }
  }

  val featuredPlugins = remember(plugins) {
    plugins.filter { it.isFeatured }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("plugin_marketplace_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Title & Subtitle
    item {
      Column {
        Text(
          text = "Plugin Marketplace",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Verified PocketMine-MP plugins and server extensions",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setMarketplaceSearch(it) },
        placeholder = { Text("Search plugins by name, tag, or author...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
          .testTag("marketplace_search_input")
      )
    }

    // Category Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val categories = listOf(
          Pair(PluginCategory.ALL, "All"),
          Pair(PluginCategory.ADMINISTRATION, "Administration"),
          Pair(PluginCategory.ECONOMY, "Economy"),
          Pair(PluginCategory.PERFORMANCE, "Performance"),
          Pair(PluginCategory.CHAT_SECURITY, "Security & Auth"),
          Pair(PluginCategory.GAMEPLAY, "Gameplay"),
          Pair(PluginCategory.DEVELOPER_TOOLS, "Dev Tools")
        )
        items(categories) { (cat, label) ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { viewModel.setMarketplaceCategory(cat) },
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.testTag("plugin_category_${cat.name.lowercase()}")
          )
        }
      }
    }

    // Featured Plugins Banner (show only when viewing All without active search)
    if (selectedCategory == PluginCategory.ALL && searchQuery.isEmpty()) {
      item {
        EuiSectionHeader(title = "Featured & Recommended", badge = "Poggit Verified")
      }

      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(featuredPlugins) { featured ->
            FeaturedPluginCard(
              plugin = featured,
              onToggleInstall = { viewModel.togglePlugin(featured.id) }
            )
          }
        }
      }
    }

    // Main Plugin Listing
    item {
      EuiSectionHeader(
        title = "Available Plugins",
        badge = "${filteredPlugins.size} Listed"
      )
    }

    items(filteredPlugins, key = { it.id }) { plugin ->
      PluginListingCard(
        plugin = plugin,
        onToggleInstall = { viewModel.togglePlugin(plugin.id) }
      )
    }
  }
}

@Composable
private fun FeaturedPluginCard(
  plugin: PluginInfo,
  onToggleInstall: () -> Unit
) {
  val icon = getPluginIcon(plugin.category)
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
    modifier = Modifier
      .width(260.dp)
      .height(160.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
            }
          }
          Column {
            Text(plugin.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(plugin.version, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
          Text("Featured", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
      }

      Text(
        text = plugin.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("↓ ${plugin.downloads}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(
          onClick = onToggleInstall,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (plugin.isInstalled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
            contentColor = if (plugin.isInstalled) MaterialTheme.colorScheme.onSurface else Color.White
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
          modifier = Modifier.defaultMinSize(minHeight = 32.dp)
        ) {
          if (plugin.isInstalled) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Installed", style = MaterialTheme.typography.labelSmall)
          } else {
            Text("Install", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
private fun PluginListingCard(
  plugin: PluginInfo,
  onToggleInstall: () -> Unit
) {
  val icon = getPluginIcon(plugin.category)
  val statusColors = LocalEuiStatusColors.current

  EuiCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = plugin.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = plugin.version,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = "by ${plugin.author} • ↓ ${plugin.downloads} downloads",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = plugin.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Compatibility badge
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = plugin.compatibility,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          // Install / Installed / Update Button
          if (plugin.hasUpdate) {
            Button(
              onClick = onToggleInstall,
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = statusColors.warning,
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .testTag("update_plugin_${plugin.id}")
            ) {
              Text("Update Available", style = MaterialTheme.typography.labelMedium)
            }
          } else if (plugin.isInstalled) {
            OutlinedButton(
              onClick = onToggleInstall,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .testTag("plugin_installed_${plugin.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = statusColors.success,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                "Installed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          } else {
            Button(
              onClick = onToggleInstall,
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
              ),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .testTag("install_plugin_${plugin.id}")
            ) {
              Text("Install", style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }
  }
}

private fun getPluginIcon(category: PluginCategory): ImageVector = when (category) {
  PluginCategory.ECONOMY -> Icons.Outlined.Paid
  PluginCategory.ADMINISTRATION -> Icons.Outlined.AdminPanelSettings
  PluginCategory.PERFORMANCE -> Icons.Outlined.Speed
  PluginCategory.CHAT_SECURITY -> Icons.Outlined.Security
  PluginCategory.GAMEPLAY -> Icons.Outlined.SportsEsports
  PluginCategory.DEVELOPER_TOOLS -> Icons.Outlined.Terminal
  else -> Icons.Outlined.Extension
}
