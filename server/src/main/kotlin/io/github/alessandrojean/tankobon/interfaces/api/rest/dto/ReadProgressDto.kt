package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Durational
import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.validation.DateRangeValidation
import io.github.alessandrojean.tankobon.infrastructure.validation.NullOrUuid
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.annotation.Nullable
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID
import java.time.LocalDateTime

data class ReadProgressEntityDto(
  override val id: String,
  override val attributes: ReadProgressAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionReadProgress>>? = null,
) : EntityDto<ReferenceExpansionReadProgress> {
  @Schema(type = "string", allowableValues = ["READ_PROGRESS"])
  override val type = EntityType.READ_PROGRESS
}

data class ReadProgressAttributesDto(
  val page: Int,
  @get:Nullable val startedAt: LocalDateTime? = null,
  @get:Nullable val finishedAt: LocalDateTime? = null,
  val isCompleted: Boolean = false,
  val createdAt: LocalDateTime,
) : EntityAttributesDto()

enum class ReferenceExpansionReadProgress : ReferenceExpansionEnum {
  BOOK,
  USER,
}

fun ReadProgress.toDto(
  bookAttributesDto: BookAttributesDto? = null,
  userAttributesDto: UserAttributesDto? = null,
) = ReadProgressEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOf(
    RelationDto(
      id = bookId,
      type = ReferenceExpansionReadProgress.BOOK,
      attributes = bookAttributesDto,
    ),
    RelationDto(
      id = userId,
      type = ReferenceExpansionReadProgress.USER,
      attributes = userAttributesDto,
    )
  ),
)

fun ReadProgress.toAttributesDto() = ReadProgressAttributesDto(
  page = page,
  startedAt = startedAt?.toUtcTimeZone(),
  finishedAt = finishedAt?.toUtcTimeZone(),
  isCompleted = isCompleted,
  createdAt = createdAt.toUtcTimeZone(),
)

@DateRangeValidation
data class ReadProgressCreationDto(
  @get:NotNull @get:Min(0) val page: Int,
  @get:Nullable override val startedAt: LocalDateTime? = null,
  @get:Nullable override val finishedAt: LocalDateTime? = null,
  @get:NotNull val isCompleted: Boolean,
  @get:NullOrUuid
  @get:Schema(format = "uuid", description = "It will use the `id` of the current authenticated user if not set or null")
  val user: String? = null,
  @get:NotBlank
  @get:UUID(version = [4])
  @get:Schema(format = "uuid")
  val book: String,
) : Durational

@DateRangeValidation
data class ReadProgressUpdateDto(
  @get:NotNull @get:Min(0) val page: Int,
  @get:Nullable override val startedAt: LocalDateTime? = null,
  @get:Nullable override val finishedAt: LocalDateTime? = null,
  @get:NotNull val isCompleted: Boolean,
) : Durational
