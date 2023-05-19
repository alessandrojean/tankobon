package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesAlternativeName
import io.github.alessandrojean.tankobon.domain.model.SeriesType
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.validation.Bcp47
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrBcp47
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrNotBlank
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID
import org.hibernate.validator.constraints.UniqueElements
import java.time.LocalDateTime

data class SeriesEntityDto(
  override val id: String,
  override val attributes: EntityAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionSeries>>? = null,
) : EntityDto<ReferenceExpansionSeries> {
  @Schema(type = "string", allowableValues = ["SERIES"])
  override val type = EntityType.SERIES
}

data class SeriesAttributesDto(
  val name: String,
  val description: String,
  val type: SeriesType?,
  val alternativeNames: List<SeriesAlternativeNameDto>,
  val lastNumber: String?,
  val originalLanguage: String?,
  val createdAt: LocalDateTime?,
  val modifiedAt: LocalDateTime?
) : EntityAttributesDto()

data class SeriesAlternativeNameDto(
  @get:NotBlank
  val name: String,
  @get:Bcp47
  @get:Schema(format = "bcp-47")
  val language: String,
)

enum class ReferenceExpansionSeries : ReferenceExpansionEnum {
  LIBRARY,
  SERIES_COVER,
}

fun Series.toDto(libraryAttributes: LibraryAttributesDto? = null) = SeriesEntityDto(
  id = id,
  attributes = toAttributesDto(alternativeNames),
  relationships = listOf(
    RelationDto(
      id = libraryId,
      type = ReferenceExpansionSeries.LIBRARY,
      attributes = libraryAttributes,
    )
  )
)

fun SeriesAlternativeName.toDto() = SeriesAlternativeNameDto(name, language)

fun Series.toAttributesDto(alternativeNames: List<SeriesAlternativeName>) = SeriesAttributesDto(
  name = name,
  description = description,
  type = type,
  alternativeNames = alternativeNames.map(SeriesAlternativeName::toDto),
  lastNumber = lastNumber,
  originalLanguage = originalLanguage,
  createdAt = createdAt.toUtcTimeZone(),
  modifiedAt = modifiedAt.toUtcTimeZone(),
)

data class SeriesCreationDto(
  @get:NotBlank
  val name: String,
  @get:NotNull
  val description: String,
  val type: SeriesType?,
  @get:NotNull
  @get:UniqueElements
  val alternativeNames: List<@NotNull SeriesAlternativeNameDto>,
  @get:NullOrNotBlank
  val lastNumber: String?,
  @get:NullOrBcp47
  @get:Schema(format = "bcp-47")
  val originalLanguage: String?,
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val library: String,
)

data class SeriesUpdateDto(
  @get:NotBlank
  val name: String,
  @get:NotNull
  val description: String,
  val type: SeriesType?,
  @get:NotNull
  @get:UniqueElements
  val alternativeNames: List<@NotNull SeriesAlternativeNameDto>,
  @get:NullOrNotBlank
  val lastNumber: String?,
  @get:NullOrBcp47
  @get:Schema(format = "bcp-47")
  val originalLanguage: String?,
)
