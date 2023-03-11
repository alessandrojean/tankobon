package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.swagger.v3.oas.annotations.media.Schema
import javax.money.MonetaryAmount

data class ImporterEntityDto(
  override val id: String,
  override val attributes: ImporterAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["EXTERNAL_BOOK"])
  override val type = EntityType.EXTERNAL_BOOK
}

data class ImporterAttributesDto(
  val isbn: String,
  val title: String,
  val contributors: List<ImporterContributorDto>,
  val publisher: String,
  val synopsis: String = "",
  val dimensions: DimensionsDto? = null,
  val labelPrice: MonetaryAmount? = null,
  val coverUrl: String = "",
  val pageCount: Int = 0,
) : EntityAttributesDto()

data class ImporterContributorDto(
  val name: String,
  val role: String,
)

fun ImporterBookResult.toDto() = ImporterEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = provider.name,
      type = RelationshipType.IMPORTER_SOURCE,
    )
  )
)

fun ImporterBookResult.toAttributesDto() = ImporterAttributesDto(
  isbn = isbn,
  title = title,
  contributors = contributors.map { ImporterContributorDto(it.name, it.role) },
  publisher = publisher,
  synopsis = synopsis,
  dimensions = dimensions?.let { DimensionsDto(it.widthCm, it.heightCm) },
  labelPrice = labelPrice,
  coverUrl = coverUrl,
  pageCount = pageCount,
)
