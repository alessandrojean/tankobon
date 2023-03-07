package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Person
import jakarta.validation.constraints.NotBlank

data class PersonEntityDto(
  override val id: String,
  override val attributes: PersonAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.PERSON
}

data class PersonAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Person.toDto(libraryAttributesDto: LibraryAttributesDto? = null) = PersonEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributesDto,
    )
  )
)

fun Person.toAttributesDto() = PersonAttributesDto(name, description)

data class PersonCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val library: String,
)

data class PersonUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
