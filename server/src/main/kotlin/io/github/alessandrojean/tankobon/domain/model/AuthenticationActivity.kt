package io.github.alessandrojean.tankobon.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class AuthenticationActivity(
  val userId: String? = null,
  val email: String? = null,
  val ip: String? = null,
  val userAgent: String? = null,
  val success: Boolean,
  val error: String? = null,
  val timestamp: LocalDateTime = LocalDateTime.now(),
  val source: String? = null,

  val id: String = UUID.randomUUID().toString(),
)
