package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Store
import jakarta.validation.constraints.NotBlank

data class StoreEntityDto(
  override val id: String,
  override val attributes: StoreAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.STORE
}

data class StoreAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Store.toDto(libraryAttributes: LibraryAttributesDto? = null) = StoreEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = id,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributes
    )
  )
)

fun Store.toAttributesDto() = StoreAttributesDto(name, description)

data class StoreCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val library: String,
)

data class StoreUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
