package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Preference
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class PreferenceEntityDto(
  @get:Schema(type = "string", format = "")
  override val id: String,
  override val attributes: PreferenceAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  @Schema(type = "string", allowableValues = ["PREFERENCE"])
  override val type = EntityType.PREFERENCE
}

data class PreferenceAttributesDto(
  val key: String,
  val value: String,
) : EntityAttributesDto()

fun Preference.toDto() = PreferenceEntityDto(
  id = key,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(id = userId, type = RelationshipType.USER)
  )
)

fun Preference.toAttributesDto() = PreferenceAttributesDto(
  key = key,
  value = value,
)

data class PreferenceCreationUpdateDto(
  @get:NotBlank
  val key: String,
  @get:NotBlank
  val value: String,
)
