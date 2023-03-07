package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.BookContributor

data class BookContributorEntityDto(
  override val id: String,
  override val attributes: BookContributorAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.CONTRIBUTOR
}

data class BookContributorAttributesDto(
  val role: String,
  val person: String,
) : EntityAttributesDto()

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
  role: String = "",
  person: String = ""
) = BookContributorAttributesDto(
  role = role,
  person = person,
)
