package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.ISBN
import org.hibernate.validator.constraints.UUID
import javax.money.MonetaryAmount

data class ImporterEntityDto(
  @get:Schema(format = "")
  override val id: String,
  override val attributes: ImporterAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionImporter>>? = null,
) : EntityDto<ReferenceExpansionImporter> {
  @Schema(type = "string", allowableValues = ["EXTERNAL_BOOK"])
  override val type = EntityType.EXTERNAL_BOOK
}

data class ImporterAttributesDto(
  @get:Schema(format = "isbn", example = "9788545702870")
  val isbn: String,
  val title: String,
  val contributors: List<ImporterContributorDto>,
  val publisher: String,
  val synopsis: String = "",
  val dimensions: DimensionsDto? = null,
  val labelPrice: MonetaryAmount? = null,
  val coverUrl: String? = null,
  val pageCount: Int = 0,
  val url: String? = null,
) : EntityAttributesDto()

data class ImporterContributorDto(
  val name: String,
  val role: String,
)

enum class ReferenceExpansionImporter : ReferenceExpansionEnum {
  IMPORTER_SOURCE,
}

data class ImporterSourceEntityDto(
  override val id: String,
  override val attributes: ImporterSourceAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionImporterSource>>? = null,
) : EntityDto<ReferenceExpansionImporterSource> {
  @Schema(type = "string", allowableValues = ["IMPORTER_SOURCE"])
  override val type = EntityType.IMPORTER_SOURCE
}

data class ImporterSourceAttributesDto(
  val name: String,
  val url: String,
  @get:Schema(
    description = "A simple multi language description in Markdown format",
    example = """{"en-US": "English description", "pt-BR": "Portuguese description"}""",
  )
  val description: Map<String, String>,
  @get:Schema(format = "bcp-47")
  val language: String,
) : EntityAttributesDto()

enum class ReferenceExpansionImporterSource : ReferenceExpansionEnum

fun ImporterProvider.toDto() = ImporterSourceEntityDto(
  id = key.name,
  attributes = toAttributesDto(),
)

fun ImporterProvider.toAttributesDto() = ImporterSourceAttributesDto(
  name = name,
  url = url,
  description = description,
  language = language,
)

fun ImporterBookResult.toDto(importerSourceAttributes: ImporterSourceAttributesDto? = null) = ImporterEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = provider.name,
      type = ReferenceExpansionImporter.IMPORTER_SOURCE,
      attributes = importerSourceAttributes,
    ),
  ),
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
  url = url,
)

data class ImportDto(
  val source: ImporterSource,
  @get:ISBN
  @get:Schema(format = "isbn", example = "9788545702870")
  val isbn: String,
  @get:NotBlank val id: String,
  @get:NotBlank
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val collection: String,
)
