package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class Series(
  val name: String,

  val id: String = UUID.randomUUID().toString(),
  override val libraryId: String = "",
  val description: String = "",

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : LibraryItem, Auditable, Serializable
