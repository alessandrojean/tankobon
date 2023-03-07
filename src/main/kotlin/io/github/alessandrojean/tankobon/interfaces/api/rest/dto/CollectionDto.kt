package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Collection
import jakarta.validation.constraints.NotBlank

data class CollectionEntityDto(
  override val id: String,
  override val attributes: CollectionAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.COLLECTION
}

data class CollectionAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Collection.toDto(libraryAttributes: LibraryAttributesDto? = null) = CollectionEntityDto(
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

fun Collection.toAttributesDto() = CollectionAttributesDto(name, description)

data class CollectionCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val library: String,
)

data class CollectionUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
