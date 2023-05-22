package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

interface ResponseDto {
  val result: ResultDto
}

enum class ResultDto {
  OK, ERROR
}

data class ErrorResponseDto(val errors: List<ErrorDto>) : ResponseDto {
  @Schema(type = "string", allowableValues = ["ERROR"])
  override val result = ResultDto.ERROR
}

@JsonInclude(Include.NON_NULL)
data class ErrorDto(
  val id: String,
  val status: Int,
  val title: String,
  val details: String,
  val stackTrace: String? = null,
)

abstract class SuccessResponseDto<T>(val data: T) : ResponseDto {
  @Schema(type = "string", allowableValues = ["OK"])
  override val result = ResultDto.OK

  abstract val response: SuccessResponseTypeDto
}

enum class SuccessResponseTypeDto {
  ENTITY, COLLECTION
}

class SuccessEntityResponseDto<T : EntityDto<*>>(data: T) : SuccessResponseDto<T>(data) {
  @Schema(type = "string", allowableValues = ["ENTITY"])
  override val response = SuccessResponseTypeDto.ENTITY
}

open class SuccessCollectionResponseDto<T : EntityDto<*>>(data: Collection<T>) :
  SuccessResponseDto<Collection<T>>(data) {
  @Schema(type = "string", allowableValues = ["COLLECTION"])
  override val response = SuccessResponseTypeDto.COLLECTION
}

@JsonInclude(Include.NON_NULL)
open class SuccessPaginatedCollectionResponseDto<T : EntityDto<*>>(
  data: Collection<T>,
  val pagination: PaginationDto,
) : SuccessCollectionResponseDto<T>(data)

data class PaginationDto(
  val currentPage: Int? = null,
  val totalPages: Int? = null,
  val totalElements: Long? = null,
)

open class EntityAttributesDto

@JsonInclude(Include.NON_NULL)
interface EntityDto<R : ReferenceExpansionEnum> {
  @get:Schema(format = "uuid")
  val id: String
  val type: EntityType
  val attributes: EntityAttributesDto
  var relationships: List<RelationDto<R>>?
}

@JsonInclude(Include.NON_NULL)
data class RelationDto<R : ReferenceExpansionEnum>(
  @get:Schema(format = "uuid")
  val id: String,
  val type: R,
  val attributes: EntityAttributesDto? = null,
)

enum class EntityType {
  COLLECTION,
  LIBRARY,
  USER,
  CONTRIBUTOR_ROLE,
  PERSON,
  PUBLISHER,
  SERIES,
  STORE,
  TAG,
  BOOK,
  CONTRIBUTOR,
  READ_PROGRESS,
  EXTERNAL_BOOK,
  IMPORTER_SOURCE,
  AUTHENTICATION_ACTIVITY,
  PREFERENCE,
}

interface ReferenceExpansionEnum

enum class RelationshipType {
  COLLECTION,
  LIBRARY,
  USER,
  OWNER,
  LIBRARY_SHARING,
  PUBLISHER,
  TAG,
  CONTRIBUTOR,
  SERIES,
  STORE,
  BOOK,
  CONTRIBUTOR_ROLE,
  PERSON,
  IMPORTER_SOURCE,
  COVER_ART,
  AVATAR,
  PERSON_PICTURE,
  PUBLISHER_PICTURE,
  PREVIOUS_BOOK,
  NEXT_BOOK,
  SERIES_COVER,
}

fun <T, R : EntityDto<*>> Page<T>.toSuccessCollectionResponseDto(mapper: (T) -> R) =
  SuccessPaginatedCollectionResponseDto(
    data = content.map(mapper),
    pagination = PaginationDto(
      currentPage = this.number,
      totalPages = totalPages,
      totalElements = totalElements,
    ),
  )

fun <T : EntityDto<*>> Page<T>.toSuccessCollectionResponseDto() = toSuccessCollectionResponseDto { it }

fun <T> Page<T>.toPaginationDto() = PaginationDto(number, totalPages, totalElements)
