package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrUuid
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class LibraryEntityDto(
  override val id: String,
  override val attributes: LibraryAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["LIBRARY"])
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
  @get:NullOrUuid
  @get:Schema(format = "uuid", description = "It will use the `id` of the current authenticated user if not set or null")
  val owner: String? = null
)

data class LibraryUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val owner: String,
  @get:Schema(type = "array", format = "uuid")
  val sharedUsers: Set<@UUID(version = [4]) String> = emptySet()
)
