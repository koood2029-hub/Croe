package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServerRepository(private val scope: CoroutineScope) {

  private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
  private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

  private val _servers = MutableStateFlow<List<ServerModel>>(initialServers())
  val servers: StateFlow<List<ServerModel>> = _servers.asStateFlow()

  private val _selectedServerId = MutableStateFlow("srv-prod-01")
  val selectedServerId: StateFlow<String> = _selectedServerId.asStateFlow()

  private val _plugins = MutableStateFlow<List<PluginInfo>>(initialPlugins())
  val plugins: StateFlow<List<PluginInfo>> = _plugins.asStateFlow()

  private val _addons = MutableStateFlow<List<AddonPack>>(initialAddons())
  val addons: StateFlow<List<AddonPack>> = _addons.asStateFlow()

  private val _consoleLogs = MutableStateFlow<Map<String, List<LogEntry>>>(initialLogs())
  val consoleLogs: StateFlow<Map<String, List<LogEntry>>> = _consoleLogs.asStateFlow()

  private val _notifications = MutableStateFlow<List<ServerActivity>>(initialNotifications())
  val notifications: StateFlow<List<ServerActivity>> = _notifications.asStateFlow()

  init {
    startMetricsTelemetry()
  }

  fun selectServer(id: String) {
    _selectedServerId.value = id
  }

  fun getSelectedServer(): ServerModel {
    return _servers.value.find { it.id == _selectedServerId.value }
      ?: _servers.value.first()
  }

  fun toggleServerPower(serverId: String) {
    val server = _servers.value.find { it.id == serverId } ?: return
    when (server.status) {
      ServerStatus.ONLINE -> stopServer(serverId)
      ServerStatus.OFFLINE -> startServer(serverId)
      else -> { /* in transition */ }
    }
  }

  fun startServer(serverId: String) {
    updateServerStatus(serverId, ServerStatus.STARTING)
    appendLog(serverId, LogLevel.INFO, "Starting PocketMine-MP Bedrock dedicated server v1.21.50...")
    appendLog(serverId, LogLevel.INFO, "Loading pocketmine.yml and server.properties...")
    
    scope.launch(Dispatchers.Default) {
      delay(2000)
      updateServerStatus(serverId, ServerStatus.ONLINE)
      appendLog(serverId, LogLevel.INFO, "Server started on UDP port 19132 [IPv4 & IPv6 enabled]. Done in 1.482s!")
      appendNotification(
        ServerActivity(
          id = System.currentTimeMillis().toString(),
          timestamp = "Just now",
          message = "Server [${getServerName(serverId)}] transitioned to ONLINE",
          actor = "System Orchestrator",
          category = "System"
        )
      )
    }
  }

  fun stopServer(serverId: String) {
    updateServerStatus(serverId, ServerStatus.STOPPING)
    appendLog(serverId, LogLevel.INFO, "Received SIGTERM. Stopping server gracefully...")
    appendLog(serverId, LogLevel.INFO, "Saving level 'world' and closing network sessions...")

    scope.launch(Dispatchers.Default) {
      delay(1800)
      updateServerStatus(serverId, ServerStatus.OFFLINE)
      appendLog(serverId, LogLevel.INFO, "Dedicated server process stopped. All resources freed.")
      appendNotification(
        ServerActivity(
          id = System.currentTimeMillis().toString(),
          timestamp = "Just now",
          message = "Server [${getServerName(serverId)}] gracefully stopped",
          actor = "Cloud Admin",
          category = "System"
        )
      )
    }
  }

  fun restartServer(serverId: String) {
    updateServerStatus(serverId, ServerStatus.RESTARTING)
    appendLog(serverId, LogLevel.WARN, "Restart initiated by Cloud Admin. Cycling process...")

    scope.launch(Dispatchers.Default) {
      delay(1500)
      appendLog(serverId, LogLevel.INFO, "Process exited. Re-spawning PocketMine daemon container...")
      delay(1800)
      updateServerStatus(serverId, ServerStatus.ONLINE)
      appendLog(serverId, LogLevel.INFO, "Restart completed. Server listening on UDP port 19132.")
      appendNotification(
        ServerActivity(
          id = System.currentTimeMillis().toString(),
          timestamp = "Just now",
          message = "Server [${getServerName(serverId)}] restarted successfully",
          actor = "Cloud Admin",
          category = "System"
        )
      )
    }
  }

  fun executeConsoleCommand(serverId: String, commandText: String) {
    val clean = commandText.trim()
    if (clean.isEmpty()) return

    val cmd = if (clean.startsWith("/")) clean else "/$clean"
    appendLog(serverId, LogLevel.COMMAND, "CONSOLE: $cmd")

    scope.launch(Dispatchers.Default) {
      delay(150)
      val response = when {
        cmd.startsWith("/help") -> "Showing help: /stop, /reload, /kick, /ban, /op, /whitelist, /time, /status, /say"
        cmd.startsWith("/say") -> "[Server] ${cmd.removePrefix("/say").trim()}"
        cmd.startsWith("/status") -> "PocketMine-MP 5.14.0 | TPS: 20.0 (100%) | Memory: 320 MB (PHP 8.2.14 JIT)"
        cmd.startsWith("/time") -> "Set world time to 1000 (Day)"
        cmd.startsWith("/whitelist add") -> "Added player to cloud whitelist"
        cmd.startsWith("/whitelist") -> "Whitelist is currently active. 42 players registered."
        cmd.startsWith("/op") -> "Granted operator permissions to specified player"
        cmd.startsWith("/kick") -> "Kicked player: Session terminated by admin"
        cmd.startsWith("/version") -> "This server is running PocketMine-MP version 5.14.0 for Minecraft: Bedrock Edition v1.21.50"
        else -> "Command '$cmd' executed successfully in server thread."
      }
      appendLog(serverId, LogLevel.INFO, response)
    }
  }

  fun updateServerProperties(serverId: String, newProps: ServerProperties) {
    _servers.update { list ->
      list.map { if (it.id == serverId) it.copy(properties = newProps) else it }
    }
    appendLog(serverId, LogLevel.INFO, "server.properties updated and saved to persistent cloud volume.")
  }

  fun createBackup(serverId: String) {
    val newSnapshot = BackupSnapshot(
      id = "snap-${System.currentTimeMillis() % 100000}",
      timestamp = dateTimeFormat.format(Date()),
      sizeMb = 142.8f,
      type = "Manual",
      status = "Healthy"
    )
    _servers.update { list ->
      list.map {
        if (it.id == serverId) it.copy(backups = listOf(newSnapshot) + it.backups) else it
      }
    }
    appendNotification(
      ServerActivity(
        id = System.currentTimeMillis().toString(),
        timestamp = "Just now",
        message = "Manual snapshot created for [${getServerName(serverId)}]",
        actor = "Cloud Admin",
        category = "Backup"
      )
    )
  }

  fun kickPlayer(serverId: String, xuid: String) {
    _servers.update { list ->
      list.map { server ->
        if (server.id == serverId) {
          val player = server.players.find { it.xuid == xuid }
          if (player != null) {
            appendLog(serverId, LogLevel.WARN, "Kicked player ${player.username} (xuid: $xuid)")
          }
          val remaining = server.players.filter { it.xuid != xuid }
          server.copy(players = remaining, currentPlayers = remaining.size)
        } else server
      }
    }
  }

  fun togglePlayerOp(serverId: String, xuid: String) {
    _servers.update { list ->
      list.map { server ->
        if (server.id == serverId) {
          val updated = server.players.map {
            if (it.xuid == xuid) it.copy(isOp = !it.isOp) else it
          }
          server.copy(players = updated)
        } else server
      }
    }
  }

  fun togglePluginInstall(pluginId: String) {
    _plugins.update { list ->
      list.map {
        if (it.id == pluginId) it.copy(isInstalled = !it.isInstalled, hasUpdate = false) else it
      }
    }
  }

  fun toggleAddonPack(addonId: String) {
    _addons.update { list ->
      list.map {
        if (it.id == addonId) it.copy(isEnabled = !it.isEnabled) else it
      }
    }
  }

  fun uninstallAddonPack(addonId: String) {
    _addons.update { list ->
      list.filter { it.id != addonId }
    }
  }

  fun addNewServer(name: String, region: String, machine: String, port: Int): String {
    val newId = "srv-${System.currentTimeMillis() % 10000}"
    val newServer = ServerModel(
      id = newId,
      name = name,
      bedrockVersion = "v1.21.50",
      pocketMineVersion = "PM-MP 5.14.0",
      ipAddress = "srv-${newId}.mc-bedrock.cloud",
      port = port,
      status = ServerStatus.ONLINE,
      currentPlayers = 1,
      maxPlayers = 40,
      region = region,
      gcpInstanceId = "pc-gcp-${(1000..9999).random()}",
      machineType = machine,
      resources = ServerResources(
        cpuPercent = 14.5f,
        ramUsedGb = 1.8f,
        ramTotalGb = 8.0f,
        storageUsedGb = 4.2f,
        storageTotalGb = 64.0f,
        networkInMb = 1.2f,
        networkOutMb = 2.4f,
        uptimeString = "0d 0h 5m"
      ),
      worlds = listOf(
        WorldInfo("w-1", "world", true, "12345678", 42.0f, "Default")
      ),
      backups = listOf(
        BackupSnapshot("snap-init", dateTimeFormat.format(Date()), 42.0f, "Automated", "Healthy")
      )
    )
    _servers.update { listOf(newServer) + it }
    _selectedServerId.value = newId
    appendNotification(
      ServerActivity(
        id = System.currentTimeMillis().toString(),
        timestamp = "Just now",
        message = "New server instance [$name] provisioned on Google Cloud",
        actor = "Cloud Setup Wizard",
        category = "System"
      )
    )
    return newId
  }

  private fun getServerName(serverId: String): String {
    return _servers.value.find { it.id == serverId }?.name ?: "Server"
  }

  private fun updateServerStatus(serverId: String, status: ServerStatus) {
    _servers.update { list ->
      list.map { if (it.id == serverId) it.copy(status = status) else it }
    }
  }

  private fun appendLog(serverId: String, level: LogLevel, message: String) {
    val time = dateFormat.format(Date())
    val entry = LogEntry(
      id = System.currentTimeMillis(),
      timestamp = time,
      level = level,
      message = message
    )
    _consoleLogs.update { map ->
      val current = map[serverId] ?: emptyList()
      map + (serverId to (current + entry).takeLast(250))
    }
  }

  private fun appendNotification(activity: ServerActivity) {
    _notifications.update { listOf(activity) + it.take(20) }
  }

  private fun startMetricsTelemetry() {
    scope.launch(Dispatchers.Default) {
      while (true) {
        delay(4000)
        _servers.update { list ->
          list.map { server ->
            if (server.status == ServerStatus.ONLINE) {
              val cpuDelta = ((Math.random() * 4) - 2).toFloat()
              val newCpu = (server.resources.cpuPercent + cpuDelta).coerceIn(12.0f, 68.0f)
              val ramDelta = ((Math.random() * 0.1) - 0.05).toFloat()
              val newRam = (server.resources.ramUsedGb + ramDelta).coerceIn(2.0f, 7.2f)
              val netInDelta = ((Math.random() * 0.4) - 0.2).toFloat()
              val newNetIn = (server.resources.networkInMb + netInDelta).coerceIn(0.8f, 15.0f)
              val netOutDelta = ((Math.random() * 0.6) - 0.3).toFloat()
              val newNetOut = (server.resources.networkOutMb + netOutDelta).coerceIn(1.5f, 22.0f)

              server.copy(
                resources = server.resources.copy(
                  cpuPercent = String.format(Locale.US, "%.1f", newCpu).toFloat(),
                  ramUsedGb = String.format(Locale.US, "%.2f", newRam).toFloat(),
                  networkInMb = String.format(Locale.US, "%.1f", newNetIn).toFloat(),
                  networkOutMb = String.format(Locale.US, "%.1f", newNetOut).toFloat()
                )
              )
            } else {
              server
            }
          }
        }
      }
    }
  }

  private fun initialServers(): List<ServerModel> = listOf(
    ServerModel(
      id = "srv-prod-01",
      name = "Survival Bedrock Realm [Prod]",
      bedrockVersion = "v1.21.50",
      pocketMineVersion = "PM-MP 5.14.0",
      ipAddress = "play.mc-bedrock.cloud",
      port = 19132,
      status = ServerStatus.ONLINE,
      currentPlayers = 18,
      maxPlayers = 50,
      region = "us-central1 (Iowa)",
      gcpInstanceId = "pc-vm-849204",
      machineType = "e2-standard-2 (2 vCPU, 8 GB RAM)",
      resources = ServerResources(
        cpuPercent = 24.0f,
        cpuCores = 4,
        ramUsedGb = 3.20f,
        ramTotalGb = 8.0f,
        storageUsedGb = 18.4f,
        storageTotalGb = 64.0f,
        networkInMb = 4.2f,
        networkOutMb = 7.8f,
        uptimeString = "14d 6h 22m",
        slaPercent = 99.98f
      ),
      properties = ServerProperties(
        serverName = "PocketCore Survival Realm",
        serverPort = 19132,
        maxPlayers = 50,
        gamemode = "Survival",
        difficulty = "Normal",
        viewDistance = 16,
        allowCheats = false,
        pvp = true,
        whiteListEnabled = true
      ),
      worlds = listOf(
        WorldInfo("w-default", "world", true, "94820194829104", 124.5f, "Default"),
        WorldInfo("w-nether", "nether", false, "94820194829104", 48.2f, "Hell"),
        WorldInfo("w-end", "the_end", false, "94820194829104", 18.9f, "End")
      ),
      players = listOf(
        PlayerInfo("x-101", "Alex_Bedrock", 24, isOp = true, isWhitelisted = true, ipAddress = "172.56.21.84"),
        PlayerInfo("x-102", "SteveCraft99", 42, isOp = false, isWhitelisted = true, ipAddress = "68.192.11.4"),
        PlayerInfo("x-103", "DiamondMiner", 18, isOp = false, isWhitelisted = true, ipAddress = "24.180.99.12"),
        PlayerInfo("x-104", "EnderQueen", 35, isOp = false, isWhitelisted = true, ipAddress = "73.204.18.90"),
        PlayerInfo("x-105", "RedstoneTech", 51, isOp = false, isWhitelisted = true, ipAddress = "98.114.77.30")
      ),
      backups = listOf(
        BackupSnapshot("snap-01", "Today at 14:00", 191.6f, "Automated", "Healthy"),
        BackupSnapshot("snap-02", "Today at 08:00", 189.4f, "Automated", "Healthy"),
        BackupSnapshot("snap-03", "Yesterday at 20:00", 186.2f, "Automated", "Healthy"),
        BackupSnapshot("snap-04", "Yesterday at 14:00", 184.0f, "Manual", "Healthy")
      ),
      activities = listOf(
        ServerActivity("act-1", "12m ago", "Player [Alex_Bedrock] authenticated via Xbox Live", "Auth Proxy", "Player"),
        ServerActivity("act-2", "2h ago", "Automated hourly backup completed (191.6 MB uploaded to Cloud Storage)", "Backup Daemon", "Backup"),
        ServerActivity("act-3", "4h ago", "Plugin [PurePerms] reloaded configuration successfully", "Server Core", "System"),
        ServerActivity("act-4", "1d ago", "Server memory cache garbage collection performed (-340 MB)", "PHP Engine", "System")
      )
    ),
    ServerModel(
      id = "srv-pvp-02",
      name = "Minigames Alpha [PvP]",
      bedrockVersion = "v1.21.50",
      pocketMineVersion = "PM-MP 5.14.0",
      ipAddress = "minigames.mc-bedrock.cloud",
      port = 19133,
      status = ServerStatus.ONLINE,
      currentPlayers = 32,
      maxPlayers = 80,
      region = "us-east1 (S. Carolina)",
      gcpInstanceId = "pc-vm-920141",
      machineType = "e2-standard-4 (4 vCPU, 16 GB RAM)",
      resources = ServerResources(
        cpuPercent = 48.2f,
        cpuCores = 4,
        ramUsedGb = 6.40f,
        ramTotalGb = 16.0f,
        storageUsedGb = 32.1f,
        storageTotalGb = 128.0f,
        networkInMb = 9.8f,
        networkOutMb = 14.2f,
        uptimeString = "5d 18h 10m",
        slaPercent = 99.95f
      ),
      properties = ServerProperties(
        serverName = "PocketCore Minigames Hub",
        serverPort = 19133,
        maxPlayers = 80,
        gamemode = "Adventure",
        difficulty = "Hard",
        viewDistance = 12,
        allowCheats = false,
        pvp = true
      ),
      worlds = listOf(
        WorldInfo("w-hub", "hub_lobby", true, "minigames2026", 88.0f, "Flat"),
        WorldInfo("w-arena1", "skywars_map", false, "skywars100", 64.5f, "Void")
      ),
      players = listOf(
        PlayerInfo("x-201", "PvPMasterX", 19, isOp = false, isWhitelisted = true),
        PlayerInfo("x-202", "ArcherGirl", 28, isOp = false, isWhitelisted = true)
      ),
      backups = listOf(
        BackupSnapshot("snap-pvp-1", "Today at 10:00", 152.0f, "Automated", "Healthy")
      )
    ),
    ServerModel(
      id = "srv-creative-03",
      name = "Creative Builder Hub",
      bedrockVersion = "v1.21.50",
      pocketMineVersion = "PM-MP 5.14.0",
      ipAddress = "creative.mc-bedrock.cloud",
      port = 19134,
      status = ServerStatus.OFFLINE,
      currentPlayers = 0,
      maxPlayers = 30,
      region = "europe-west1 (Belgium)",
      gcpInstanceId = "pc-vm-381920",
      machineType = "e2-medium (1 vCPU, 4 GB RAM)",
      resources = ServerResources(
        cpuPercent = 0.0f,
        cpuCores = 2,
        ramUsedGb = 0.0f,
        ramTotalGb = 4.0f,
        storageUsedGb = 14.8f,
        storageTotalGb = 32.0f,
        networkInMb = 0.0f,
        networkOutMb = 0.0f,
        uptimeString = "Stopped",
        slaPercent = 100.0f
      ),
      properties = ServerProperties(
        serverName = "Creative Architecture Realm",
        serverPort = 19134,
        maxPlayers = 30,
        gamemode = "Creative",
        difficulty = "Peaceful",
        viewDistance = 20,
        allowCheats = true,
        pvp = false
      )
    )
  )

  private fun initialPlugins(): List<PluginInfo> = listOf(
    PluginInfo(
      id = "pl-econ",
      name = "EconomyAPI",
      version = "v5.7.2",
      author = "onebone & poggit team",
      description = "Enterprise core economy library for PocketMine-MP servers, supporting seamless transactions and multi-currency.",
      category = PluginCategory.ECONOMY,
      downloads = "1.2M",
      isInstalled = true,
      hasUpdate = false,
      isFeatured = true,
      memoryFootprintMb = 3.8f
    ),
    PluginInfo(
      id = "pl-perms",
      name = "PurePerms",
      version = "v1.4.3",
      author = "ProjectInfinity",
      description = "Comprehensive multi-group permission and RBAC management system with inheritance and world-specific nodes.",
      category = PluginCategory.ADMINISTRATION,
      downloads = "980K",
      isInstalled = true,
      hasUpdate = false,
      isFeatured = true,
      memoryFootprintMb = 5.2f
    ),
    PluginInfo(
      id = "pl-auth",
      name = "SimpleAuth",
      version = "v2.1.0",
      author = "PocketMine Core",
      description = "Secure password registration, hash validation, and session caching for Bedrock and Xbox Live players.",
      category = PluginCategory.CHAT_SECURITY,
      downloads = "820K",
      isInstalled = true,
      hasUpdate = false,
      memoryFootprintMb = 4.1f
    ),
    PluginInfo(
      id = "pl-clearlagg",
      name = "ClearLagg Bedrock",
      version = "v3.0.4",
      author = "CloudOptimize",
      description = "Real-time entity culling, TPS stabilization, sub-chunk garbage collection, and periodic RAM defragmentation.",
      category = PluginCategory.PERFORMANCE,
      downloads = "640K",
      isInstalled = false,
      hasUpdate = false,
      isFeatured = true,
      memoryFootprintMb = 2.4f
    ),
    PluginInfo(
      id = "pl-fasttransfer",
      name = "FastTransfer",
      version = "v2.0.1",
      author = "Shoghicp",
      description = "High-speed zero-delay player transfer between Bedrock cloud cluster nodes and sub-servers.",
      category = PluginCategory.ADMINISTRATION,
      downloads = "450K",
      isInstalled = false,
      hasUpdate = false,
      memoryFootprintMb = 1.9f
    ),
    PluginInfo(
      id = "pl-formapi",
      name = "FormAPI",
      version = "v2.1.2",
      author = "jojoe77777",
      description = "Native Bedrock GUI modal, simple, and custom form creation library for interactive player menus.",
      category = PluginCategory.DEVELOPER_TOOLS,
      downloads = "1.8M",
      isInstalled = true,
      hasUpdate = true,
      memoryFootprintMb = 3.1f
    ),
    PluginInfo(
      id = "pl-scorehud",
      name = "ScoreHud",
      version = "v6.1.0",
      author = "Ifera",
      description = "Customizable enterprise sidebar scoreboard HUD displaying player ping, money, ranks, and server TPS.",
      category = PluginCategory.GAMEPLAY,
      downloads = "530K",
      isInstalled = false,
      hasUpdate = false,
      memoryFootprintMb = 4.6f
    ),
    PluginInfo(
      id = "pl-slapper",
      name = "Slapper",
      version = "v2.2.0",
      author = "jojoe77777",
      description = "Create customizable NPC entities that execute commands or route players upon interaction.",
      category = PluginCategory.GAMEPLAY,
      downloads = "710K",
      isInstalled = false,
      hasUpdate = false,
      memoryFootprintMb = 3.5f
    )
  )

  private fun initialAddons(): List<AddonPack> = listOf(
    AddonPack(
      id = "addon-bp-01",
      name = "Enterprise Security Wardens",
      version = "1.4.0",
      description = "Custom automated guard behavior pack for Bedrock realm protection and anti-grief boundaries.",
      type = AddonType.BEHAVIOR_PACK,
      uuid = "4c8f2a10-721a-4f51-b012-98420184a102",
      sizeMb = 6.8f,
      author = "Mojang Partner Studio",
      isInstalled = true,
      isEnabled = true
    ),
    AddonPack(
      id = "addon-bp-02",
      name = "Advanced Machinery & Automation",
      version = "2.1.2",
      description = "Adds industrial generators, pipes, item filters, and energy transport systems to Minecraft Bedrock.",
      type = AddonType.BEHAVIOR_PACK,
      uuid = "8f12a450-c192-49e0-a982-120938491024",
      sizeMb = 14.2f,
      author = "BedrockTech Labs",
      isInstalled = true,
      isEnabled = false
    ),
    AddonPack(
      id = "addon-rp-01",
      name = "Minimalist Clean UI (Faithful 32x)",
      version = "3.2.0",
      description = "Clean high-definition textures optimized for enterprise server rendering and high frame rates.",
      type = AddonType.RESOURCE_PACK,
      uuid = "99827104-e820-410a-b284-774029104812",
      sizeMb = 28.5f,
      author = "Faithful Team",
      isInstalled = true,
      isEnabled = true
    ),
    AddonPack(
      id = "addon-rp-02",
      name = "Low-Latency PvP Particles",
      version = "1.0.8",
      description = "Lightweight particle pack reducing render latency during intense minigames and player combat.",
      type = AddonType.RESOURCE_PACK,
      uuid = "21049182-3819-4820-a012-482910492810",
      sizeMb = 4.1f,
      author = "Competitive Bedrock",
      isInstalled = true,
      isEnabled = true
    )
  )

  private fun initialLogs(): Map<String, List<LogEntry>> = mapOf(
    "srv-prod-01" to listOf(
      LogEntry(1, "16:30:02", LogLevel.INFO, "Server thread", "PocketMine-MP Bedrock dedicated server v1.21.50 initialized"),
      LogEntry(2, "16:30:03", LogLevel.INFO, "Server thread", "Loading resource packs: 2 active packs mounted"),
      LogEntry(3, "16:30:04", LogLevel.INFO, "Server thread", "Loading plugins: EconomyAPI, PurePerms, SimpleAuth, FormAPI"),
      LogEntry(4, "16:30:05", LogLevel.INFO, "Server thread", "[EconomyAPI] Loaded multi-currency database in 0.082s"),
      LogEntry(5, "16:30:06", LogLevel.INFO, "Server thread", "[PurePerms] Registered 4 permission groups: default, vip, mod, admin"),
      LogEntry(6, "16:30:08", LogLevel.INFO, "Server thread", "Default world 'world' loaded (Seed: 94820194829104)"),
      LogEntry(7, "16:30:09", LogLevel.INFO, "Server thread", "RakNet server listening on 0.0.0.0:19132 [IPv4] and [::]:19132 [IPv6]"),
      LogEntry(8, "16:30:10", LogLevel.INFO, "Server thread", "Server is now open for player connections! Done (6.812s)"),
      LogEntry(9, "16:35:12", LogLevel.INFO, "Network", "Player Alex_Bedrock [IP: 172.56.21.84] logged in with entity id 102"),
      LogEntry(10, "16:38:40", LogLevel.INFO, "Network", "Player SteveCraft99 [IP: 68.192.11.4] logged in with entity id 103"),
      LogEntry(11, "16:40:15", LogLevel.WARN, "Watchdog", "Sub-chunk loading exceeded 50ms at chunk (12, -4)"),
      LogEntry(12, "16:42:00", LogLevel.INFO, "Backup Daemon", "Cloud snapshot backup created and verified healthy")
    ),
    "srv-pvp-02" to listOf(
      LogEntry(1, "15:00:01", LogLevel.INFO, "Server thread", "Minigames Alpha PvP server container started on port 19133"),
      LogEntry(2, "15:00:04", LogLevel.INFO, "Server thread", "Arena maps loaded: skywars_map (64.5 MB)"),
      LogEntry(3, "15:00:05", LogLevel.INFO, "Server thread", "32 active players currently connected to lobby")
    ),
    "srv-creative-03" to listOf(
      LogEntry(1, "09:00:00", LogLevel.INFO, "Server thread", "Server instance offline. Ready to start.")
    )
  )

  private fun initialNotifications(): List<ServerActivity> = listOf(
    ServerActivity("nt-1", "5m ago", "Automated hourly backup verified healthy across all nodes", "Cloud Storage", "Backup"),
    ServerActivity("nt-2", "18m ago", "Player Alex_Bedrock connected to Survival Bedrock Realm", "Auth Proxy", "Player"),
    ServerActivity("nt-3", "1h ago", "Google Cloud quota check: 3 of 10 compute instances active", "GCP IAM", "System"),
    ServerActivity("nt-4", "4h ago", "SSL certificate renewed for play.mc-bedrock.cloud", "Cloud Security", "Security")
  )
}
