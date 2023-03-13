package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class PublisherEntityDto(
  override val id: String,
  override val attributes: PublisherAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["PUBLISHER"])
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
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class PublisherUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
