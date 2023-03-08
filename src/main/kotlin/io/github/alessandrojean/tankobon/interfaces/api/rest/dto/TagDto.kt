package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Tag
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class TagEntityDto(
  override val id: String,
  override val attributes: TagAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["TAG"])
  override val type = EntityType.TAG
}

data class TagAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Tag.toDto(libraryAttributes: LibraryAttributesDto? = null) = TagEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributes
    )
  )
)

fun Tag.toAttributesDto() = TagAttributesDto(name, description)

data class TagCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class TagUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
