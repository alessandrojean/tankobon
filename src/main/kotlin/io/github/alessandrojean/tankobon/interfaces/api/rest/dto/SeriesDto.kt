package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Series
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

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
) : EntityAttributesDto()

fun Series.toDto(libraryAttributes: LibraryAttributesDto? = null) = SeriesEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributes,
    )
  )
)

fun Series.toAttributesDto() = SeriesAttributesDto(name, description)

data class SeriesCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class SeriesUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
