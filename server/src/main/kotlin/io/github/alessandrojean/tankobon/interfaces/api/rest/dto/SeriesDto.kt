package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesAlternativeName
import io.github.alessandrojean.tankobon.domain.model.SeriesType
import io.github.alessandrojean.tankobon.infrastructure.validation.Bcp47
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID
import org.hibernate.validator.constraints.UniqueElements

data class SeriesEntityDto(
  override val id: String,
  override val attributes: EntityAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["SERIES"])
  override val type = EntityType.SERIES
}

data class SeriesAttributesDto(
  val name: String,
  val description: String,
  val type: SeriesType?,
  val alternativeNames: List<SeriesAlternativeNameDto>,
) : EntityAttributesDto()

data class SeriesAlternativeNameDto(
  @get:NotBlank
  val name: String,
  @get:Bcp47
  @get:Schema(format = "bcp-47")
  val language: String,
)

fun Series.toDto(libraryAttributes: LibraryAttributesDto? = null) = SeriesEntityDto(
  id = id,
  attributes = toAttributesDto(alternativeNames),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributes,
    )
  )
)

fun SeriesAlternativeName.toDto() = SeriesAlternativeNameDto(name, language)

fun Series.toAttributesDto(alternativeNames: List<SeriesAlternativeName>) = SeriesAttributesDto(
  name = name,
  description = description,
  type = type,
  alternativeNames = alternativeNames.map(SeriesAlternativeName::toDto),
)

data class SeriesCreationDto(
  @get:NotBlank
  val name: String,
  @get:NotNull
  val description: String,
  val type: SeriesType?,
  @get:NotNull
  @get:UniqueElements
  val alternativeNames: List<@NotNull SeriesAlternativeNameDto>,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class SeriesUpdateDto(
  @get:NotBlank
  val name: String,
  @get:NotNull
  val description: String,
  val type: SeriesType?,
)
