package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class ReadProgress(
  val page: Int,

  override val startedAt: LocalDateTime? = null,
  override val finishedAt: LocalDateTime? = null,
  val isCompleted: Boolean = false,

  val id: String = UUID.randomUUID().toString(),
  val bookId: String = "",
  val userId: String = "",

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : Auditable, Durational, Serializable
