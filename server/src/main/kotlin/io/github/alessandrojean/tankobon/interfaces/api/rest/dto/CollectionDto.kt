package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class CollectionEntityDto(
  override val id: String,
  override val attributes: CollectionAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionCollection>>? = null,
) : EntityDto<ReferenceExpansionCollection> {
  @Schema(type = "string", allowableValues = ["COLLECTION"])
  override val type = EntityType.COLLECTION
}

data class CollectionAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

enum class ReferenceExpansionCollection : ReferenceExpansionEnum {
  LIBRARY,
}

fun Collection.toDto(libraryAttributes: LibraryAttributesDto? = null) = CollectionEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = ReferenceExpansionCollection.LIBRARY,
      attributes = libraryAttributes,
    )
  )
)

fun Collection.toAttributesDto() = CollectionAttributesDto(name, description)

data class CollectionCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class CollectionUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
