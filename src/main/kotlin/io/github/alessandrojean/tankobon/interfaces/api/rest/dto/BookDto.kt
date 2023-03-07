package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class BookEntityDto(
  override val id: String,
  override val attributes: BookAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.BOOK
}

data class BookAttributesDto(
  val code: String,
  val barcode: String?,
  val title: String,
  val paidPrice: MonetaryValueDto,
  val labelPrice: MonetaryValueDto,
  val dimensions: DimensionsDto,
  val isInLibrary: Boolean,
  val number: String,
  val pageCount: Int,
  val synopsis: String,
  val notes: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val boughtAt: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val billedAt: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val arrivedAt: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val createdAt: LocalDateTime?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val modifiedAt: LocalDateTime?
) : EntityAttributesDto()

fun Book.toAttributesDto() = BookAttributesDto(
  code = code,
  barcode = barcode,
  title = title,
  paidPrice = MonetaryValueDto(
    currency = paidPrice.currency,
    value = paidPrice.value,
  ),
  labelPrice = MonetaryValueDto(
    currency = labelPrice.currency,
    value = labelPrice.value,
  ),
  dimensions = DimensionsDto(
    widthCm = dimensions.widthCm,
    heightCm = dimensions.heightCm,
  ),
  isInLibrary = isInLibrary,
  number = number,
  pageCount = pageCount,
  synopsis = synopsis,
  notes = notes,
  boughtAt = boughtAt,
  billedAt = billedAt,
  arrivedAt = arrivedAt,
  createdAt = createdAt,
  modifiedAt = modifiedAt,
)

data class BookCreationDto(
  val collection: String,
  @get:NullOrNotBlank val store: String? = null,
  @get:NullOrNotBlank val series: String? = null,
  val contributors: List<BookContributorCreationDto>,
  val publishers: Set<@NotBlank String>,
  val tags: Set<@NotBlank String>? = null,
  @get:NotBlank val code: String,
  @get:NullOrNotBlank val barcode: String? = null,
  @get:NotBlank val title: String,
  val paidPrice: MonetaryValueDto,
  val labelPrice: MonetaryValueDto,
  val dimensions: DimensionsDto,
  val isInLibrary: Boolean,
  val number: String,
  val pageCount: Int,
  val synopsis: String,
  val notes: String,
  @get:NullOrNotBlank val boughtAt: LocalDateTime?,
  @get:NullOrNotBlank val billedAt: LocalDateTime?,
  @get:NullOrNotBlank val arrivedAt: LocalDateTime?,
)

typealias BookUpdateDto = BookCreationDto

data class BookContributorCreationDto(
  @get:NotBlank val person: String,
  @get:NotBlank val role: String,
)

data class DimensionsDto(
  val widthCm: Float,
  val heightCm: Float,
)
