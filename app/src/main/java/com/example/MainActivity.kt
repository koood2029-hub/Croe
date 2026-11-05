package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ServerActivity
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.theme.PocketCoreTheme
import com.example.ui.viewmodel.PocketCoreViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: PocketCoreViewModel = viewModel()
      val isDarkThemePref by viewModel.isDarkTheme.collectAsState()
      val systemDark = isSystemInDarkTheme()
      val useDarkTheme = isDarkThemePref ?: systemDark

      PocketCoreTheme(darkTheme = useDarkTheme) {
        PocketCoreApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketCoreApp(viewModel: PocketCoreViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val confirmDialog by viewModel.confirmActionDialog.collectAsState()
  val showNotifications by viewModel.showNotificationsSheet.collectAsState()
  val notifications by viewModel.notifications.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.userFeedback.collectLatest { msg ->
      snackbarHostState.showSnackbar(
        message = msg,
        duration = SnackbarDuration.Short
      )
    }
  }

  // Handle system back navigation
  BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
    viewModel.navigateBack()
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTablet = maxWidth >= 720.dp

    if (isTablet) {
      // Tablet layout: Side NavigationRail + Main Content Area
      Row(modifier = Modifier.fillMaxSize()) {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
        ) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            header = {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(38.dp)
                  .padding(top = 8.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Outlined.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            },
            modifier = Modifier.fillMaxHeight()
          ) {
            val navItems = listOf(
              NavItem(Screen.DASHBOARD, "Home", Icons.Default.Home, Icons.Outlined.Home),
              NavItem(Screen.SERVERS, "Servers", Icons.Default.Dns, Icons.Outlined.Dns),
              NavItem(Screen.MARKETPLACE, "Plugins", Icons.Default.Storefront, Icons.Outlined.Storefront),
              NavItem(Screen.ADDONS, "Add-ons", Icons.Default.Extension, Icons.Outlined.Extension),
              NavItem(Screen.SETTINGS, "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
            )

            navItems.forEach { item ->
              NavigationRailItem(
                selected = currentScreen == item.screen,
                onClick = { viewModel.navigateTo(item.screen) },
                icon = {
                  Icon(
                    imageVector = if (currentScreen == item.screen) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title
                  )
                },
                label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
              )
            }
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) {
          Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
              ScreenRouter(currentScreen = currentScreen, viewModel = viewModel)
            }
          }
        }
      }
    } else {
      // Mobile layout: Standard M3 Scaffold with Bottom Navigation
      val showBottomBar = currentScreen != Screen.CLOUD_WIZARD && currentScreen != Screen.SERVER_MANAGEMENT

      Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
          if (showBottomBar) {
            EnterpriseBottomNav(
              currentScreen = currentScreen,
              onSelect = { viewModel.navigateTo(it) }
            )
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          ScreenRouter(currentScreen = currentScreen, viewModel = viewModel)
        }
      }
    }
  }

  // Confirmation modal dialog for stopping/restarting servers
  if (confirmDialog != null) {
    val (message, onConfirm) = confirmDialog!!
    EuiConfirmationDialog(
      title = "Confirm Action",
      message = message,
      confirmButtonText = "Proceed",
      dismissButtonText = "Cancel",
      isDestructive = message.contains("Stop", ignoreCase = true),
      onConfirm = {
        onConfirm()
        viewModel.dismissDialog()
      },
      onDismiss = { viewModel.dismissDialog() }
    )
  }

  // Notification Sheet
  if (showNotifications) {
    ModalBottomSheet(
      onDismissRequest = { viewModel.toggleNotificationsSheet(false) },
      containerColor = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 10.dp)
          .navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Cloud Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          TextButton(onClick = { viewModel.toggleNotificationsSheet(false) }) {
            Text("Dismiss", style = MaterialTheme.typography.labelMedium)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
        ) {
          items(notifications) { item ->
            NotificationCard(item = item)
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun ScreenRouter(currentScreen: Screen, viewModel: PocketCoreViewModel) {
  when (currentScreen) {
    Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
    Screen.SERVERS -> ServersScreen(viewModel = viewModel)
    Screen.SERVER_MANAGEMENT -> ServerManagementScreen(viewModel = viewModel)
    Screen.MARKETPLACE -> MarketplaceScreen(viewModel = viewModel)
    Screen.ADDONS -> AddonsScreen(viewModel = viewModel)
    Screen.CONSOLE -> ConsoleScreen(viewModel = viewModel)
    Screen.CLOUD_WIZARD -> CloudSetupWizardScreen(viewModel = viewModel)
    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
  }
}

private data class NavItem(
  val screen: Screen,
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
)

@Composable
private fun EnterpriseBottomNav(
  currentScreen: Screen,
  onSelect: (Screen) -> Unit
) {
  val navItems = listOf(
    NavItem(Screen.DASHBOARD, "Home", Icons.Default.Home, Icons.Outlined.Home),
    NavItem(Screen.SERVERS, "Servers", Icons.Default.Dns, Icons.Outlined.Dns),
    NavItem(Screen.MARKETPLACE, "Plugins", Icons.Default.Storefront, Icons.Outlined.Storefront),
    NavItem(Screen.ADDONS, "Add-ons", Icons.Default.Extension, Icons.Outlined.Extension),
    NavItem(Screen.SETTINGS, "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
  )

  Surface(
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth()
  ) {
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface,
      tonalElevation = 0.dp,
      modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars)
        .testTag("main_bottom_nav")
    ) {
      navItems.forEach { item ->
        val selected = currentScreen == item.screen
        NavigationBarItem(
          selected = selected,
          onClick = { onSelect(item.screen) },
          icon = {
            Icon(
              imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
              contentDescription = item.title,
              modifier = Modifier.size(20.dp)
            )
          },
          label = {
            Text(
              text = item.title,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          modifier = Modifier.testTag("nav_${item.title.lowercase()}")
        )
      }
    }
  }
}

@Composable
private fun NotificationCard(item: ServerActivity) {
  val statusColors = LocalEuiStatusColors.current

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      val icon = when (item.category) {
        "Backup" -> Icons.Outlined.CloudDone
        "Player" -> Icons.Outlined.Person
        "Security" -> Icons.Outlined.Security
        else -> Icons.Outlined.Info
      }

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (item.category == "Backup") statusColors.success else MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(20.dp)
      )

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.message,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${item.actor} • ${item.timestamp}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
