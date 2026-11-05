package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ServerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
  DASHBOARD,
  SERVERS,
  SERVER_MANAGEMENT,
  MARKETPLACE,
  ADDONS,
  CONSOLE,
  CLOUD_WIZARD,
  SETTINGS
}

class PocketCoreViewModel : ViewModel() {

  val repository = ServerRepository(viewModelScope)

  private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _previousScreen = MutableStateFlow<Screen?>(null)

  private val _selectedServerId = repository.selectedServerId
  val selectedServerId: StateFlow<String> = _selectedServerId

  val servers: StateFlow<List<ServerModel>> = repository.servers
  val plugins: StateFlow<List<PluginInfo>> = repository.plugins
  val addons: StateFlow<List<AddonPack>> = repository.addons
  val consoleLogs: StateFlow<Map<String, List<LogEntry>>> = repository.consoleLogs
  val notifications: StateFlow<List<ServerActivity>> = repository.notifications

  // UI state
  private val _serverSearchQuery = MutableStateFlow("")
  val serverSearchQuery: StateFlow<String> = _serverSearchQuery.asStateFlow()

  private val _serverFilterStatus = MutableStateFlow<ServerStatus?>(null)
  val serverFilterStatus: StateFlow<ServerStatus?> = _serverFilterStatus.asStateFlow()

  private val _marketplaceCategory = MutableStateFlow(PluginCategory.ALL)
  val marketplaceCategory: StateFlow<PluginCategory> = _marketplaceCategory.asStateFlow()

  private val _marketplaceSearch = MutableStateFlow("")
  val marketplaceSearch: StateFlow<String> = _marketplaceSearch.asStateFlow()

  private val _addonActiveTab = MutableStateFlow(0) // 0: Behavior, 1: Resource, 2: Installed
  val addonActiveTab: StateFlow<Int> = _addonActiveTab.asStateFlow()

  private val _logFilterLevel = MutableStateFlow(LogLevel.ALL)
  val logFilterLevel: StateFlow<LogLevel> = _logFilterLevel.asStateFlow()

  private val _logSearchQuery = MutableStateFlow("")
  val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

  private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null = system default
  val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

  private val _showNotificationsSheet = MutableStateFlow(false)
  val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

  // Wizard state
  private val _wizardState = MutableStateFlow(CloudSetupState())
  val wizardState: StateFlow<CloudSetupState> = _wizardState.asStateFlow()

  // Management active tab (0: Overview, 1: Config, 2: Worlds, 3: Players, 4: Backups, 5: Console)
  private val _managementTab = MutableStateFlow(0)
  val managementTab: StateFlow<Int> = _managementTab.asStateFlow()

  // Toast / feedback message flow
  private val _userFeedback = MutableSharedFlow<String>()
  val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

  // Dialog states
  private val _confirmActionDialog = MutableStateFlow<Pair<String, () -> Unit>?>(null)
  val confirmActionDialog: StateFlow<Pair<String, () -> Unit>?> = _confirmActionDialog.asStateFlow()

  fun navigateTo(screen: Screen) {
    if (_currentScreen.value != screen) {
      _previousScreen.value = _currentScreen.value
      _currentScreen.value = screen
    }
  }

  fun navigateBack() {
    val prev = _previousScreen.value
    if (prev != null) {
      _currentScreen.value = prev
      _previousScreen.value = null
    } else {
      _currentScreen.value = Screen.DASHBOARD
    }
  }

  fun selectAndManageServer(serverId: String, tab: Int = 0) {
    repository.selectServer(serverId)
    _managementTab.value = tab
    navigateTo(Screen.SERVER_MANAGEMENT)
  }

  fun setManagementTab(tabIndex: Int) {
    _managementTab.value = tabIndex
  }

  fun setServerSearch(query: String) {
    _serverSearchQuery.value = query
  }

  fun setServerFilter(status: ServerStatus?) {
    _serverFilterStatus.value = status
  }

  fun setMarketplaceCategory(cat: PluginCategory) {
    _marketplaceCategory.value = cat
  }

  fun setMarketplaceSearch(query: String) {
    _marketplaceSearch.value = query
  }

  fun setAddonTab(index: Int) {
    _addonActiveTab.value = index
  }

  fun setLogFilter(level: LogLevel) {
    _logFilterLevel.value = level
  }

  fun setLogSearch(query: String) {
    _logSearchQuery.value = query
  }

  fun toggleNotificationsSheet(show: Boolean) {
    _showNotificationsSheet.value = show
  }

  fun setDarkModePreference(dark: Boolean?) {
    _isDarkTheme.value = dark
  }

  fun emitFeedback(msg: String) {
    viewModelScope.launch {
      _userFeedback.emit(msg)
    }
  }

  // Server operations
  fun toggleServerPower(serverId: String) {
    val server = servers.value.find { it.id == serverId } ?: return
    if (server.status == ServerStatus.ONLINE) {
      _confirmActionDialog.value = Pair("Stop server [${server.name}]? Active players will be disconnected.") {
        repository.stopServer(serverId)
        emitFeedback("Stopping ${server.name}...")
      }
    } else if (server.status == ServerStatus.OFFLINE) {
      repository.startServer(serverId)
      emitFeedback("Starting ${server.name}...")
    }
  }

  fun requestRestart(serverId: String) {
    val server = servers.value.find { it.id == serverId } ?: return
    _confirmActionDialog.value = Pair("Restart server [${server.name}]? Server will cycle in ~3 seconds.") {
      repository.restartServer(serverId)
      emitFeedback("Restarting ${server.name}...")
    }
  }

  fun dismissDialog() {
    _confirmActionDialog.value = null
  }

  fun executeConsoleCommand(serverId: String, cmd: String) {
    repository.executeConsoleCommand(serverId, cmd)
  }

  fun saveServerProperties(serverId: String, props: ServerProperties) {
    repository.updateServerProperties(serverId, props)
    emitFeedback("Configuration saved to server.properties")
  }

  fun createBackupNow(serverId: String) {
    repository.createBackup(serverId)
    emitFeedback("Cloud snapshot initiated successfully")
  }

  fun kickPlayer(serverId: String, xuid: String) {
    repository.kickPlayer(serverId, xuid)
    emitFeedback("Player kicked from server")
  }

  fun togglePlayerOp(serverId: String, xuid: String) {
    repository.togglePlayerOp(serverId, xuid)
    emitFeedback("Player permissions updated")
  }

  fun togglePlugin(pluginId: String) {
    repository.togglePluginInstall(pluginId)
    val plugin = plugins.value.find { it.id == pluginId }
    val isNowInstalled = !(plugin?.isInstalled ?: false)
    emitFeedback(if (isNowInstalled) "Installed ${plugin?.name}" else "Uninstalled ${plugin?.name}")
  }

  fun toggleAddon(addonId: String) {
    repository.toggleAddonPack(addonId)
    emitFeedback("Add-on status toggled")
  }

  fun uninstallAddon(addonId: String) {
    repository.uninstallAddonPack(addonId)
    emitFeedback("Add-on uninstalled")
  }

  // Cloud Setup Wizard actions
  fun startCloudWizard() {
    _wizardState.value = CloudSetupState(
      currentStep = WizardStep.WELCOME,
      deploymentSteps = listOf(
        DeploymentStep("Provision Google Compute VM", "Allocating e2-standard-2 in us-central1 (Iowa)"),
        DeploymentStep("Configure VPC Firewall Rules", "Opening Bedrock UDP port 19132 and TLS 443"),
        DeploymentStep("Install PHP 8.2 & PocketMine-MP 5", "Deploying high-performance containerized runtime"),
        DeploymentStep("Initialize World & Configs", "Generating level data and server.properties"),
        DeploymentStep("Run Cloud Health Verification", "Checking RakNet latency and network throughput")
      )
    )
    navigateTo(Screen.CLOUD_WIZARD)
  }

  fun setWizardStep(step: WizardStep) {
    _wizardState.update { it.copy(currentStep = step) }
  }

  fun updateWizardConfig(
    serverName: String,
    region: String,
    machineType: String,
    diskSize: Int,
    version: String
  ) {
    _wizardState.update {
      it.copy(
        serverName = serverName,
        region = region,
        machineType = machineType,
        diskSizeGb = diskSize,
        bedrockVersion = version
      )
    }
  }

  fun setWizardTermsAccepted(accepted: Boolean) {
    _wizardState.update { it.copy(termsAccepted = accepted) }
  }

  fun triggerDeployment() {
    val state = _wizardState.value
    if (!state.termsAccepted) {
      emitFeedback("Please review and accept the cloud deployment terms.")
      return
    }

    _wizardState.update {
      it.copy(
        currentStep = WizardStep.DEPLOYING,
        isDeploying = true,
        deploymentProgress = 0.05f
      )
    }

    viewModelScope.launch {
      val stepCount = state.deploymentSteps.size
      for (i in 0 until stepCount) {
        _wizardState.update { current ->
          val updatedSteps = current.deploymentSteps.mapIndexed { idx, s ->
            when {
              idx < i -> s.copy(isCompleted = true, isInProgress = false)
              idx == i -> s.copy(isInProgress = true, isCompleted = false)
              else -> s.copy(isCompleted = false, isInProgress = false)
            }
          }
          current.copy(
            deploymentSteps = updatedSteps,
            deploymentProgress = (i.toFloat() + 0.3f) / stepCount
          )
        }
        delay(900)
      }

      // Complete all steps
      val deployedId = repository.addNewServer(
        name = state.serverName,
        region = state.region,
        machine = state.machineType,
        port = 19135
      )

      _wizardState.update { current ->
        val finalSteps = current.deploymentSteps.map { it.copy(isCompleted = true, isInProgress = false) }
        current.copy(
          currentStep = WizardStep.SUCCESS,
          isDeploying = false,
          deploymentProgress = 1.0f,
          deploymentSteps = finalSteps,
          deployedIp = "34.122.90.114",
          deployedPort = 19135
        )
      }
      emitFeedback("Cloud Server [${state.serverName}] successfully deployed!")
    }
  }
}
