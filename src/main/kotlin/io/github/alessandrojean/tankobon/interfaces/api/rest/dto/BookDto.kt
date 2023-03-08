package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrUuid
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.validator.constraints.UUID
import java.time.LocalDateTime

data class BookEntityDto(
  override val id: String,
  override val attributes: BookAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["BOOK"])
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
  @get:NotBlank
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val collection: String,
  @get:NullOrUuid
  @get:Schema(format = "uuid", description = "Store where the book was bought")
  val store: String? = null,
  @get:NullOrUuid
  @get:Schema(format = "uuid")
  val series: String? = null,
  @get:NotEmpty
  val contributors: List<BookContributorCreationDto>,
  @get:NotEmpty
  @get:Schema(type = "array", format = "uuid")
  val publishers: Set<@UUID(version = [4]) String>,
  @get:Schema(type = "array", format = "uuid")
  val tags: Set<@UUID(version = [4]) String>? = null,
  @get:NotBlank
  @get:Schema(description = "An unique code identifier to the book, such as its ISBN-13")
  val code: String,
  @get:NullOrNotBlank
  @get:Schema(description = "The barcode printed in the book cover")
  val barcode: String? = null,
  @get:NotBlank val title: String,
  val paidPrice: MonetaryValueDto,
  val labelPrice: MonetaryValueDto,
  val dimensions: DimensionsDto,
  @get:Schema(description = "If the book is a future and planned acquisition, set as `false`")
  val isInLibrary: Boolean,
  @get:Schema(description = "If the book is part of a series, this will control the order")
  val number: String,
  val pageCount: Int,
  val synopsis: String,
  @get:Schema(description = "Personal user notes about the book")
  val notes: String,
  @get:NullOrNotBlank val boughtAt: LocalDateTime?,
  @get:NullOrNotBlank
  @get:Schema(description = "Date of payment, useful for pre-orders like Amazon ones")
  val billedAt: LocalDateTime?,
  @get:NullOrNotBlank
  @get:Schema(description = "Date of delivery and arrival of the book")
  val arrivedAt: LocalDateTime?,
)

typealias BookUpdateDto = BookCreationDto

data class BookContributorCreationDto(
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val person: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val role: String,
)

data class DimensionsDto(
  val widthCm: Float,
  val heightCm: Float,
)
