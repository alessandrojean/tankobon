package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class ContributorRoleEntityDto(
  override val id: String,
  override val attributes: ContributorRoleAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["CONTRIBUTOR_ROLE"])
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
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class ContributorRoleUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
