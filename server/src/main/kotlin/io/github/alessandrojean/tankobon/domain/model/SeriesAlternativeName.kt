package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class SeriesAlternativeName(
  override val name: String,
  override val language: String,

  val id: String = UUID.randomUUID().toString(),
  val seriesId: String = "",

  val createdAt: LocalDateTime = LocalDateTime.now(),
): AlternativeName, Serializable
