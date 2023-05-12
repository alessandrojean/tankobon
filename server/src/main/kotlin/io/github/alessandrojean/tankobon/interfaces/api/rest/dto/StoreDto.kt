package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Store
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.UUID

data class StoreEntityDto(
  override val id: String,
  override val attributes: StoreAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionStore>>? = null,
) : EntityDto<ReferenceExpansionStore> {
  @Schema(type = "string", allowableValues = ["STORE"])
  override val type = EntityType.STORE
}

data class StoreAttributesDto(
  val name: String,
  val description: String,
) : EntityAttributesDto()

enum class ReferenceExpansionStore : ReferenceExpansionEnum {
  LIBRARY,
}

fun Store.toDto(libraryAttributes: LibraryAttributesDto? = null) = StoreEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = id,
      type = ReferenceExpansionStore.LIBRARY,
      attributes = libraryAttributes
    )
  )
)

fun Store.toAttributesDto() = StoreAttributesDto(name, description)

data class StoreCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class StoreUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
)
