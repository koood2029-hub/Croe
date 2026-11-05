package com.example.data.model

enum class PluginCategory {
  ALL,
  ADMINISTRATION,
  ECONOMY,
  PERFORMANCE,
  CHAT_SECURITY,
  GAMEPLAY,
  DEVELOPER_TOOLS
}

data class PluginInfo(
  val id: String,
  val name: String,
  val version: String,
  val author: String,
  val description: String,
  val category: PluginCategory,
  val compatibility: String = "PM-MP 5.x / Bedrock v1.21.x",
  val downloads: String,
  val isInstalled: Boolean = false,
  val hasUpdate: Boolean = false,
  val isFeatured: Boolean = false,
  val memoryFootprintMb: Float = 4.2f
)
