package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.UrlMultipleHosts
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
import org.hibernate.validator.constraints.UUID

data class PublisherEntityDto(
  override val id: String,
  override val attributes: PublisherAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionPublisher>>? = null,
) : EntityDto<ReferenceExpansionPublisher> {
  @Schema(type = "string", allowableValues = ["PUBLISHER"])
  override val type = EntityType.PUBLISHER
}

data class PublisherAttributesDto(
  val name: String,
  val description: String,
  val links: PublisherLinksDto,
) : EntityAttributesDto()

data class PublisherLinksDto(
  @get:NullOrNotBlank
  @get:URL
  val website: String? = null,
  @get:NullOrNotBlank
  @get:URL
  val store: String? = null,
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

enum class ReferenceExpansionPublisher : ReferenceExpansionEnum {
  LIBRARY,
  PUBLISHER_PICTURE,
}

fun Publisher.toDto(libraryAttributes: LibraryAttributesDto? = null) = PublisherEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = ReferenceExpansionPublisher.LIBRARY,
      attributes = libraryAttributes,
    )
  )
)

fun Publisher.toAttributesDto() = PublisherAttributesDto(
  name = name,
  description = description,
  links = PublisherLinksDto(
    website = links.website,
    store = links.store,
    twitter = links.twitter,
    instagram = links.instagram,
    facebook = links.facebook,
    youTube = links.youTube,
  )
)

data class PublisherCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  val links: PublisherLinksDto,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class PublisherUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  val links: PublisherLinksDto,
)
