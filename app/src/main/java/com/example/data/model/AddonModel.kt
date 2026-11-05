package com.example.data.model

enum class AddonType {
  BEHAVIOR_PACK,
  RESOURCE_PACK
}

data class AddonPack(
  val id: String,
  val name: String,
  val version: String,
  val description: String,
  val type: AddonType,
  val uuid: String,
  val sizeMb: Float,
  val author: String,
  val isInstalled: Boolean = true,
  val isEnabled: Boolean = true,
  val hasUpdate: Boolean = false
)
