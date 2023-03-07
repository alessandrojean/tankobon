package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Publisher
import jakarta.validation.constraints.NotBlank

data class PublisherEntityDto(
  override val id: String,
  override val attributes: PublisherAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.PUBLISHER
}

data class PublisherAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Publisher.toDto(libraryAttributes: LibraryAttributesDto? = null) = PublisherEntityDto(
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

fun Publisher.toAttributesDto() = PublisherAttributesDto(name, description)

data class PublisherCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val library: String,
)

data class PublisherUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
