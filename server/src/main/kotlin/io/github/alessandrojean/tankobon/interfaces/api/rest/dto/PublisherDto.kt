package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.DurationalCompanyYear
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.validation.CompanyYearRangeValidation
import io.github.alessandrojean.tankobon.infrastructure.validation.MaxCurrentYear
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrIso3166
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.UrlMultipleHosts
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.URL
import org.hibernate.validator.constraints.UUID
import java.time.LocalDateTime

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
  val legalName: String,
  val location: String?,
  val foundingYear: Int?,
  val dissolutionYear: Int?,
  val createdAt: LocalDateTime,
  val modifiedAt: LocalDateTime,
) : EntityAttributesDto()

data class PublisherLinksDto(
  @get:NullOrNotBlank
  @get:URL
  @get:Schema(format = "uri", nullable = true)
  val website: String? = null,
  @get:NullOrNotBlank
  @get:URL
  @get:Schema(format = "uri", nullable = true)
  val store: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["twitter.com", "mobile.twitter.com"])
  @get:Schema(format = "uri", nullable = true)
  val twitter: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["instagram.com"])
  @get:Schema(format = "uri", nullable = true)
  val instagram: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["facebook.com"])
  @get:Schema(format = "uri", nullable = true)
  val facebook: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["youtube.com"])
  @get:Schema(format = "uri", nullable = true)
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
    ),
  ),
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
  ),
  legalName = legalName,
  location = location.orEmpty().ifEmpty { null },
  foundingYear = foundingYear,
  dissolutionYear = dissolutionYear,
  createdAt = createdAt.toUtcTimeZone(),
  modifiedAt = modifiedAt.toUtcTimeZone(),
)

@CompanyYearRangeValidation
data class PublisherCreationDto(
  @get:NotBlank
  val name: String,
  @get:NotNull
  val description: String,
  @get:NotNull
  @get:Valid
  val links: PublisherLinksDto,
  @get:NotNull
  val legalName: String,
  @get:NullOrIso3166
  val location: String?,
  @get:Positive
  @get:Min(1900)
  override val foundingYear: Int?,
  @get:Positive
  @get:MaxCurrentYear
  override val dissolutionYear: Int?,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
) : DurationalCompanyYear

@CompanyYearRangeValidation
data class PublisherUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  @get:Valid
  val links: PublisherLinksDto,
  @get:NotNull
  val legalName: String,
  @get:NullOrIso3166
  val location: String?,
  @get:Positive
  @get:Min(1900)
  override val foundingYear: Int?,
  @get:Positive
  @get:MaxCurrentYear
  override val dissolutionYear: Int?,
) : DurationalCompanyYear
