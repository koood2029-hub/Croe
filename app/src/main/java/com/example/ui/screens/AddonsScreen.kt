package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.data.model.AddonPack
import com.example.data.model.AddonType
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiSectionHeader
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonsScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val addons by viewModel.addons.collectAsState()
  val activeTab by viewModel.addonActiveTab.collectAsState()

  val displayedAddons = remember(addons, activeTab) {
    when (activeTab) {
      0 -> addons.filter { it.type == AddonType.BEHAVIOR_PACK }
      1 -> addons.filter { it.type == AddonType.RESOURCE_PACK }
      else -> addons.filter { it.isInstalled && it.isEnabled }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("addons_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Bar with Upload Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Add-ons & Packs",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Manage Bedrock behavior (.mcpack) and resource packs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = { viewModel.emitFeedback("Upload dialog: Select .mcpack or .mcaddon") },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
          ),
          modifier = Modifier
            .height(44.dp)
            .testTag("upload_addon_button")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Upload Pack", style = MaterialTheme.typography.labelLarge)
        }
      }
    }

    // Tabs: Behavior Packs, Resource Packs, Installed Add-ons
    item {
      SecondaryTabRow(
        selectedTabIndex = activeTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.secondary,
        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline) },
        modifier = Modifier.fillMaxWidth()
      ) {
        val tabTitles = listOf(
          "Behavior Packs (${addons.count { it.type == AddonType.BEHAVIOR_PACK }})",
          "Resource Packs (${addons.count { it.type == AddonType.RESOURCE_PACK }})",
          "Active (${addons.count { it.isEnabled }})"
        )
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = activeTab == index,
            onClick = { viewModel.setAddonTab(index) },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium
              )
            },
            modifier = Modifier.testTag("addon_tab_$index")
          )
        }
      }
    }

    item {
      EuiSectionHeader(
        title = when (activeTab) {
          0 -> "Behavior Packs (BP)"
          1 -> "Resource Packs (RP)"
          else -> "Active Add-ons Stack"
        },
        badge = "${displayedAddons.size} Packs"
      )
    }

    if (displayedAddons.isEmpty()) {
      item {
        EuiCard {
          Text(
            text = "No add-on packs in this category.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      items(displayedAddons, key = { it.id }) { pack ->
        AddonCardItem(
          pack = pack,
          onToggle = { viewModel.toggleAddon(pack.id) },
          onUninstall = { viewModel.uninstallAddon(pack.id) },
          onUpdate = { viewModel.emitFeedback("Pack [${pack.name}] updated to latest manifest") }
        )
      }
    }
  }
}

@Composable
private fun AddonCardItem(
  pack: AddonPack,
  onToggle: () -> Unit,
  onUninstall: () -> Unit,
  onUpdate: () -> Unit
) {
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
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (pack.type == AddonType.BEHAVIOR_PACK) Icons.Outlined.Layers else Icons.Outlined.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
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
            text = pack.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Switch(
            checked = pack.isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.testTag("toggle_addon_${pack.id}")
          )
        }

        Text(
          text = "v${pack.version} • ${pack.author} • ${pack.sizeMb} MB",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = pack.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "UUID: ${pack.uuid.take(16)}...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pack.hasUpdate) {
              OutlinedButton(
                onClick = onUpdate,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, statusColors.warning),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.defaultMinSize(minHeight = 32.dp)
              ) {
                Text("Update", style = MaterialTheme.typography.labelSmall, color = statusColors.warning)
              }
            }

            OutlinedButton(
              onClick = onUninstall,
              shape = RoundedCornerShape(6.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier
                .defaultMinSize(minHeight = 32.dp)
                .testTag("uninstall_addon_${pack.id}")
            ) {
              Text("Uninstall", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}
