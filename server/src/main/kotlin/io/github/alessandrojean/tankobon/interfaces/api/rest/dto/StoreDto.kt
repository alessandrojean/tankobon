package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.StoreType
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrIso3166
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.UrlMultipleHosts
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
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
  val links: StoreLinksDto,
  val legalName: String,
  val location: String?,
  val type: StoreType?,
) : EntityAttributesDto()

data class StoreLinksDto(
  @get:NullOrNotBlank
  @get:URL
  val website: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["twitter.com", "mobile.twitter.com"])
  val twitter: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["instagram.com"])
  val instagram: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["facebook.com"])
  val facebook: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["youtube.com"])
  val youTube: String? = null,
)

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

fun Store.toAttributesDto() = StoreAttributesDto(
  name = name,
  description = description,
  links = StoreLinksDto(
    website = links.website,
    twitter = links.twitter,
    instagram = links.instagram,
    facebook = links.facebook,
    youTube = links.youTube,
  ),
  legalName = legalName,
  location = location,
  type = type,
)

data class StoreCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  @get:Valid
  val links: StoreLinksDto,
  @get:NotNull
  val legalName: String,
  @get:NullOrIso3166
  val location: String?,
  val type: StoreType?,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class StoreUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  @get:Valid
  val links: StoreLinksDto,
  @get:NotNull
  val legalName: String,
  @get:NullOrIso3166
  val location: String?,
  val type: StoreType?,
)
