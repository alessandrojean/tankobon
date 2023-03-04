package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

data class SystemStatusDto(
  val status: SystemStatus,
  val isDevelopment: Boolean,
  val isDemo: Boolean,
  val version: String,
)

enum class SystemStatus {
  OPERATIONAL, PROBLEMS, NO_DATABASE
}
