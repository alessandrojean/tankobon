package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID
import java.time.LocalDateTime

data class Collection(
  val name: String,

  val id: String = UUID.randomUUID().toString(),
  val libraryId: String = "",
  val description: String = "",

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : Auditable, Serializable
