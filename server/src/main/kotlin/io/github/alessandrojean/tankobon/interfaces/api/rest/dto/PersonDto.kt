package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.DurationalPerson
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrBcp47
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrIso3166
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.github.alessandrojean.tankobon.infrastructure.validation.PersonDateRangeValidation
import io.github.alessandrojean.tankobon.infrastructure.validation.UrlMultipleHosts
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
import org.hibernate.validator.constraints.UUID
import java.time.LocalDate
import java.time.LocalDateTime

data class PersonEntityDto(
  override val id: String,
  override val attributes: PersonAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionPerson>>? = null,
) : EntityDto<ReferenceExpansionPerson> {
  @Schema(type = "string", allowableValues = ["PERSON"])
  override val type = EntityType.PERSON
}

data class PersonAttributesDto(
  val name: String,
  val description: String,
  val links: PersonLinksDto,
  val bornAt: LocalDate?,
  val diedAt: LocalDate?,
  val nationality: String?,
  val nativeName: PersonNativeNameDto,
  val createdAt: LocalDateTime,
  val modifiedAt: LocalDateTime,
) : EntityAttributesDto()

data class PersonNativeNameDto(
  @get:NotNull
  val name: String,
  @get:NullOrBcp47
  @get:Schema(format = "bcp-47", nullable = true)
  val language: String?,
)

data class PersonLinksDto(
  @get:NullOrNotBlank
  @get:URL
  @get:Schema(format = "uri", nullable = true)
  val website: String? = null,
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
  @get:UrlMultipleHosts(allowedHosts = ["pixiv.net"])
  @get:Schema(format = "uri", nullable = true)
  val pixiv: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["deviantart.com"])
  @get:Schema(format = "uri", nullable = true)
  val deviantArt: String? = null,
  @get:NullOrNotBlank
  @get:UrlMultipleHosts(allowedHosts = ["youtube.com"])
  @get:Schema(format = "uri", nullable = true)
  val youTube: String? = null,
)

enum class ReferenceExpansionPerson : ReferenceExpansionEnum {
  LIBRARY,
  PERSON_PICTURE,
}

fun Person.toDto(libraryAttributesDto: LibraryAttributesDto? = null) = PersonEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = ReferenceExpansionPerson.LIBRARY,
      attributes = libraryAttributesDto,
    ),
  ),
)

fun Person.toAttributesDto() = PersonAttributesDto(
  name = name,
  description = description,
  links = PersonLinksDto(
    website = links.website,
    twitter = links.twitter,
    instagram = links.instagram,
    facebook = links.facebook,
    pixiv = links.pixiv,
    deviantArt = links.deviantArt,
    youTube = links.youTube,
  ),
  bornAt = bornAt,
  diedAt = diedAt,
  nationality = nationality,
  nativeName = PersonNativeNameDto(
    name = nativeName,
    language = nativeNameLanguage,
  ),
  createdAt = createdAt.toUtcTimeZone(),
  modifiedAt = modifiedAt.toUtcTimeZone(),
)

@PersonDateRangeValidation
data class PersonCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotNull
  @get:Valid
  val links: PersonLinksDto,
  override val bornAt: LocalDate?,
  override val diedAt: LocalDate?,
  @get:NullOrIso3166
  val nationality: String?,
  @get:NotNull
  @get:Valid
  val nativeName: PersonNativeNameDto,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
) : DurationalPerson

@PersonDateRangeValidation
data class PersonUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  override val bornAt: LocalDate?,
  override val diedAt: LocalDate?,
  @get:NullOrIso3166
  val nationality: String?,
  @get:NotNull
  @get:Valid
  val nativeName: PersonNativeNameDto,
  @get:NotNull
  @get:Valid
  val links: PersonLinksDto,
) : DurationalPerson
