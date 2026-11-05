package com.example.data.model

enum class ServerStatus {
  ONLINE,
  OFFLINE,
  STARTING,
  STOPPING,
  RESTARTING;

  fun isOperating(): Boolean = this == STARTING || this == STOPPING || this == RESTARTING
}

data class ServerResources(
  val cpuPercent: Float,
  val cpuCores: Int = 4,
  val ramUsedGb: Float,
  val ramTotalGb: Float = 8.0f,
  val storageUsedGb: Float,
  val storageTotalGb: Float = 64.0f,
  val networkInMb: Float,
  val networkOutMb: Float,
  val uptimeString: String = "14d 6h 22m",
  val slaPercent: Float = 99.98f
)

data class ServerProperties(
  val serverName: String = "PocketCore Dedicated Realm",
  val serverPort: Int = 19132,
  val maxPlayers: Int = 50,
  val gamemode: String = "Survival",
  val difficulty: String = "Normal",
  val viewDistance: Int = 16,
  val allowCheats: Boolean = false,
  val pvp: Boolean = true,
  val whiteListEnabled: Boolean = false,
  val onlineMode: Boolean = true,
  val subChunkLimit: Int = 12
)

data class WorldInfo(
  val id: String,
  val name: String,
  val isDefault: Boolean,
  val seed: String,
  val sizeMb: Float,
  val levelType: String = "Default"
)

data class PlayerInfo(
  val xuid: String,
  val username: String,
  val pingMs: Int,
  val isOp: Boolean = false,
  val isWhitelisted: Boolean = true,
  val ipAddress: String = "192.168.1.10"
)

data class BackupSnapshot(
  val id: String,
  val timestamp: String,
  val sizeMb: Float,
  val type: String, // "Automated", "Manual"
  val status: String = "Healthy"
)

data class ServerActivity(
  val id: String,
  val timestamp: String,
  val message: String,
  val actor: String,
  val category: String // "System", "Player", "Backup", "Security"
)

data class ServerModel(
  val id: String,
  val name: String,
  val bedrockVersion: String = "v1.21.50",
  val pocketMineVersion: String = "PM-MP 5.14.0",
  val ipAddress: String = "play.mc-bedrock.cloud",
  val port: Int = 19132,
  val status: ServerStatus = ServerStatus.ONLINE,
  val currentPlayers: Int = 18,
  val maxPlayers: Int = 50,
  val region: String = "us-central1 (Iowa)",
  val gcpInstanceId: String = "pc-vm-849204",
  val machineType: String = "e2-standard-2",
  val resources: ServerResources,
  val properties: ServerProperties = ServerProperties(),
  val worlds: List<WorldInfo> = emptyList(),
  val players: List<PlayerInfo> = emptyList(),
  val backups: List<BackupSnapshot> = emptyList(),
  val activities: List<ServerActivity> = emptyList()
)
