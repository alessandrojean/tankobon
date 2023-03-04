package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID
import java.time.LocalDateTime

data class Library(
  val name: String,
  val description: String = "",

  val id: String = UUID.randomUUID().toString(),
  val ownerId: String = "",
  val sharedUsersIds: Set<String> = emptySet(),

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt,
) : Auditable, Serializable
