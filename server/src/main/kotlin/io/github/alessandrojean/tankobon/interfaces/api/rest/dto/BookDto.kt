package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.LengthUnit
import io.github.alessandrojean.tankobon.domain.model.MassUnit
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.parser.CodeType
import io.github.alessandrojean.tankobon.infrastructure.parser.guessCodeType
import io.github.alessandrojean.tankobon.infrastructure.parser.toIsbnInformation
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrUuid
import io.github.alessandrojean.tankobon.infrastructure.validation.PositiveAmount
import io.github.alessandrojean.tankobon.infrastructure.validation.UrlMultipleHosts
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID
import org.hibernate.validator.constraints.UniqueElements
import java.time.LocalDateTime
import javax.money.MonetaryAmount

data class BookEntityDto(
  override val id: String,
  override val attributes: BookAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionBook>>? = null,
) : EntityDto<ReferenceExpansionBook> {
  @Schema(type = "string", allowableValues = ["BOOK"])
  override val type = EntityType.BOOK
}

data class BookAttributesDto(
  val code: CodeDto,
  val barcode: String?,
  val title: String,
  val subtitle: String,
  val paidPrice: MonetaryAmount,
  val labelPrice: MonetaryAmount,
  val dimensions: DimensionsDto,
  val weight: WeightDto,
  val isInLibrary: Boolean,
  val number: String,
  val pageCount: Int,
  val synopsis: String,
  val notes: String,
  val links: BookLinksDto,
  val boughtAt: LocalDateTime?,
  val billedAt: LocalDateTime?,
  val arrivedAt: LocalDateTime?,
  val createdAt: LocalDateTime?,
  val modifiedAt: LocalDateTime?,
) : EntityAttributesDto()

data class BookLinksDto(
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(
    allowedHosts = ["amazon.com", "amazon.ca", "amazon.com.br", "amazon.co.uk", "amazon.co.jp", "amazon.cn", "amazon.com.au", "amazon.com.be", "amazon.eg", "amazon.fr", "amazon.in", "amazon.it", "amazon.com.mx", "amazon.nl", "amazon.pl", "amazon.sa", "amazon.sg", "amazon.es", "amazon.se", "amazon.com.tr", "amazon.ae", "amazon.de"],
  )
  @get:Schema(format = "uri", nullable = true)
  val amazon: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["openlibrary.org"])
  @get:Schema(format = "uri", nullable = true)
  val openLibrary: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["skoob.com.br"])
  @get:Schema(format = "uri", nullable = true)
  val skoob: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["goodreads.com"])
  @get:Schema(format = "uri", nullable = true)
  val goodreads: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["guiadosquadrinhos.com"])
  @get:Schema(format = "uri", nullable = true)
  val guiaDosQuadrinhos: String? = null,
)

enum class ReferenceExpansionBook : ReferenceExpansionEnum {
  CONTRIBUTOR,
  COLLECTION,
  PUBLISHER,
  SERIES,
  STORE,
  TAG,
  LIBRARY,
  COVER_ART,
  PREVIOUS_BOOK,
  NEXT_BOOK,
}

interface CodeDto {
  val type: CodeType
  val code: String
}

data class SimpleCodeDto(
  override val type: CodeType,
  override val code: String,
) : CodeDto

data class IsbnCodeDto(
  override val type: CodeType,
  override val code: String,
  val group: Int?,
  val region: String?,
  val language: String?,
) : CodeDto

fun String.toCodeDto(): CodeDto = when (val type = guessCodeType()) {
  CodeType.ISBN_13, CodeType.ISBN_10 -> {
    val isbnInformation = toIsbnInformation()

    IsbnCodeDto(
      type = type,
      code = this,
      group = isbnInformation?.group,
      region = isbnInformation?.region,
      language = isbnInformation?.language,
    )
  }
  else -> SimpleCodeDto(type = type, code = this)
}

fun Book.toAttributesDto() = BookAttributesDto(
  code = code.toCodeDto(),
  barcode = barcode,
  title = title,
  subtitle = subtitle,
  paidPrice = paidPrice,
  labelPrice = labelPrice,
  dimensions = DimensionsDto(
    width = dimensions.width,
    height = dimensions.height,
    depth = dimensions.depth,
    unit = dimensions.unit,
  ),
  weight = WeightDto(
    value = weight.value,
    unit = weight.unit,
  ),
  isInLibrary = isInLibrary,
  number = number,
  pageCount = pageCount,
  synopsis = synopsis,
  notes = notes,
  links = BookLinksDto(
    amazon = links.amazon,
    openLibrary = links.openLibrary,
    skoob = links.skoob,
    goodreads = links.goodreads,
    guiaDosQuadrinhos = links.guiaDosQuadrinhos,
  ),
  boughtAt = boughtAt?.toUtcTimeZone(),
  billedAt = billedAt?.toUtcTimeZone(),
  arrivedAt = arrivedAt?.toUtcTimeZone(),
  createdAt = createdAt.toUtcTimeZone(),
  modifiedAt = modifiedAt.toUtcTimeZone(),
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
  @get:UniqueElements
  @get:Valid
  val contributors: List<@NotNull BookContributorCreationDto>,
  @get:NotEmpty
  @get:UniqueElements
  @get:Schema(type = "array", format = "uuid")
  val publishers: List<@UUID(version = [4]) String>,
  @get:UniqueElements
  @get:Schema(type = "array", format = "uuid")
  val tags: List<@UUID(version = [4]) String>? = null,
  @get:NotBlank
  @get:Schema(description = "An unique code identifier to the book, such as its ISBN-13")
  val code: String,
  @get:NullOrNotBlank
  @get:Schema(description = "The barcode printed in the book cover")
  val barcode: String? = null,
  @get:NotBlank val title: String,
  val subtitle: String,
  @get:NotNull
  @get:PositiveAmount
  val paidPrice: MonetaryAmount,
  @get:NotNull
  @get:PositiveAmount
  val labelPrice: MonetaryAmount,
  @get:NotNull
  @get:Valid
  val dimensions: DimensionsDto,
  @get:NotNull
  @get:Valid
  val weight: WeightDto,
  @get:Schema(description = "If the book is a future and planned acquisition, set as `false`")
  val isInLibrary: Boolean,
  @get:NotNull
  @get:Schema(description = "If the book is part of a series, this will control the order")
  val number: String,
  @get:NotNull
  @get:Min(value = 0)
  val pageCount: Int,
  @get:NotNull
  val synopsis: String,
  @get:NotNull
  @get:Schema(description = "Personal user notes about the book")
  val notes: String,
  @get:NotNull
  @get:Valid
  val links: BookLinksDto,
  @get:Nullable val boughtAt: LocalDateTime? = null,
  @get:Nullable
  @get:Schema(description = "Date of payment, useful for pre-orders like Amazon ones")
  val billedAt: LocalDateTime? = null,
  @get:Nullable
  @get:Schema(description = "Date of delivery and arrival of the book")
  val arrivedAt: LocalDateTime? = null,
)

typealias BookUpdateDto = BookCreationDto

data class BookContributorCreationDto(
  @get:NotBlank
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val person: String,
  @get:NotBlank
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val role: String,
)

data class DimensionsDto(
  @get:NotNull
  @get:Min(value = 0L)
  @get:Schema(example = "13.2")
  val width: Float,
  @get:NotNull
  @get:Min(value = 0L)
  @get:Schema(example = "20.0")
  val height: Float,
  @get:NotNull
  @get:Min(value = 0L)
  @get:Schema(example = "1.5")
  val depth: Float,
  @get:NotNull
  val unit: LengthUnit,
)

data class WeightDto(
  @get:NotNull
  @get:Min(value = 0L)
  @get:Schema(example = "0.2")
  val value: Float,
  @get:NotNull
  val unit: MassUnit,
)
