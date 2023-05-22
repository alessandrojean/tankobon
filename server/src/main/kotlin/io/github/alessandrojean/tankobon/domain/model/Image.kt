package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class Image(
  val fileName: String,
  val width: Int,
  val height: Int,
  val aspectRatio: String,
  val format: String,
  val mimeType: String,
  val timeHex: String,
  val blurHash: String,

  val id: String = UUID.randomUUID().toString(),
  val versions: Map<String, String> = emptyMap(),

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt,
) : Serializable, Auditable
