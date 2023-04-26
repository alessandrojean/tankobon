package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.BookContributor
import io.swagger.v3.oas.annotations.media.Schema

data class BookContributorEntityDto(
  override val id: String,
  override val attributes: BookContributorAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["CONTRIBUTOR"])
  override val type = EntityType.CONTRIBUTOR
}

data class BookContributorAttributesDto(
  val role: BookContributorRoleDto,
  val person: BookContributorPersonDto,
) : EntityAttributesDto()

data class BookContributorPersonDto(
  val id: String,
  val name: String,
)

data class BookContributorRoleDto(
  val id: String,
  val name: String,
)

fun BookContributor.toDto() = BookContributorEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(bookId, RelationshipType.BOOK),
    RelationDto(roleId, RelationshipType.CONTRIBUTOR_ROLE),
    RelationDto(personId, RelationshipType.PERSON)
  ),
)

fun BookContributor.toAttributesDto(
  roleId: String = "",
  roleName: String = "",
  personId: String = "",
  personName: String = ""
) = BookContributorAttributesDto(
  role = BookContributorRoleDto(
    id = roleId,
    name = roleName,
  ),
  person = BookContributorPersonDto(
    id = personId,
    name = personName,
  ),
)
