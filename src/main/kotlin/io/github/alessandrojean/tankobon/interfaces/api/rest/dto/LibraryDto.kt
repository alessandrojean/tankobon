package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Library
import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank

data class LibraryEntityDto(
  override val id: String,
  override val attributes: LibraryAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.LIBRARY
}

data class LibraryAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun Library.toDto(userAttributes: UserAttributesDto? = null) = LibraryEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = ownerId,
      type = RelationshipType.OWNER,
      attributes = userAttributes
    ),
    *sharedUsersIds
      .map { RelationDto(it, RelationshipType.LIBRARY_SHARING) }
      .toTypedArray()
  )
)

fun Library.toAttributesDto() = LibraryAttributesDto(name, description)

data class LibraryCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:Nullable val owner: String? = null
)

data class LibraryUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val owner: String,
  val sharedUsers: Set<String> = emptySet()
)
