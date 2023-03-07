package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Tag
import jakarta.validation.constraints.NotBlank

data class TagEntityDto(
  override val id: String,
  override val attributes: TagAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
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
  @get:NotBlank val library: String,
)

data class TagUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
