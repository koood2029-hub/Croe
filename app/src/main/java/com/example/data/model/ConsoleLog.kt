package com.example.data.model

enum class LogLevel {
  ALL,
  INFO,
  WARN,
  ERROR,
  COMMAND
}

data class LogEntry(
  val id: Long,
  val timestamp: String,
  val level: LogLevel,
  val thread: String = "Server thread",
  val message: String
)
