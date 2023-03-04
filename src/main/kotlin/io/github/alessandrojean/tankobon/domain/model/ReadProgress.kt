package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID
import java.time.LocalDateTime

data class ReadProgress(
  val page: Int,

  val startedAt: LocalDateTime? = null,
  val finishedAt: LocalDateTime? = null,
  val isCompleted: Boolean = false,

  val bookId: String = "",
  val userId: String = "",

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : Auditable, Serializable
