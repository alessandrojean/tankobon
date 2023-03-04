package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID
import java.time.LocalDateTime

data class BookMetadata(
  val dimensionWidthCm: Float,
  val dimensionHeightCm: Float,
  val labelPriceCurrency: String,
  val number: String = "0",

  val labelPriceValue: Float,
  val synopsis: String = "",
  val pageCount: Int = 0,

  val bookId: String = "",
  val publisherIds: List<String> = emptyList(),
  val tagIds: List<String> = emptyList(),

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : Auditable, Serializable
