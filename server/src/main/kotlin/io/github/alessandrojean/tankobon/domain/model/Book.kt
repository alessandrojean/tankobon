package io.github.alessandrojean.tankobon.domain.model

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID
import javax.money.MonetaryAmount

data class Book(
  val code: String,
  val title: String,
  val paidPrice: MonetaryAmount,
  val labelPrice: MonetaryAmount,
  val dimensions: Dimensions,

  val id: String = UUID.randomUUID().toString(),
  val collectionId: String = "",
  val storeId: String? = null,
  val seriesId: String? = null,

  val barcode: String? = null,
  val isInLibrary: Boolean = true,
  val number: String = "0",
  val pageCount: Int = 0,
  val synopsis: String = "",
  val notes: String = "",

  val source: ImporterSource? = null,
  val sourceBookId: String? = null,

  val subtitle: String = "",
  val weightKg: Float = 0f,
  val links: BookLinks = BookLinks(),

  /**
   * Assumed to be in the UTC timezone.
   */
  val boughtAt: LocalDateTime? = null,

  /**
   * Assumed to be in the UTC timezone.
   */
  val billedAt: LocalDateTime? = null,

  /**
   * Assumed to be in the UTC timezone.
   */
  val arrivedAt: LocalDateTime? = null,

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt,
) : Auditable, Serializable
