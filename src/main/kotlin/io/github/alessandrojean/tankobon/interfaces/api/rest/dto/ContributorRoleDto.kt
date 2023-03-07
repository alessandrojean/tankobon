package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import jakarta.validation.constraints.NotBlank

data class ContributorRoleEntityDto(
  override val id: String,
  override val attributes: ContributorRoleAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.CONTRIBUTOR_ROLE
}

data class ContributorRoleAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

fun ContributorRole.toDto(libraryAttributes: LibraryAttributesDto? = null) = ContributorRoleEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = RelationshipType.LIBRARY,
      attributes = libraryAttributes,
    )
  ),
)

fun ContributorRole.toAttributesDto() = ContributorRoleAttributesDto(name, description)

data class ContributorRoleCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val library: String,
)

data class ContributorRoleUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
