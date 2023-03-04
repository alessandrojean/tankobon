package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class Book(
  val code: String,
  val title: String,
  val paidPriceCurrency: String,
  val paidPriceValue: String,

  val id: String = UUID.randomUUID().toString(),
  val collectionId: String = "",
  val seriesId: String? = null,
  val storeId: String? = null,

  val barcode: String? = null,
  val isInLibrary: Boolean = true,
  val boughtAt: LocalDateTime? = null,
  val notes: String = "",

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt,
) : Auditable, Serializable
