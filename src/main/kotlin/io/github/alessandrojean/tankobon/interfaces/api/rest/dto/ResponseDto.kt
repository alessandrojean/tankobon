package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import org.springframework.data.domain.Page

open class ResponseDto(
  val result: ResultDto,
)

enum class ResultDto {
  OK, ERROR
}

data class ErrorResponseDto(
  val errors: List<ErrorDto>,
) : ResponseDto(ResultDto.ERROR)

data class ErrorDto(
  val id: String,
  val status: Int,
  val title: String,
  val details: String,
)

open class SuccessResponseDto<T>(
  val response: SuccessResponseTypeDto,
  val data: T,
) : ResponseDto(ResultDto.OK)

enum class SuccessResponseTypeDto {
  ENTITY, COLLECTION
}

class SuccessEntityResponseDto<T : EntityDto>(data: T) :
  SuccessResponseDto<T>(SuccessResponseTypeDto.ENTITY, data)

@JsonInclude(Include.NON_NULL)
class SuccessCollectionResponseDto<T : EntityDto>(
  data: Collection<T>,
  pagination: PaginationDto? = null,
) : SuccessResponseDto<Collection<T>>(SuccessResponseTypeDto.COLLECTION, data)

data class PaginationDto(
  val currentPage: Int? = null,
  val totalPages: Int? = null,
  val totalElements: Long? = null,
)

open class EntityAttributesDto

@JsonInclude(Include.NON_NULL)
interface EntityDto {
  val id: String
  val type: EntityType
  val attributes: EntityAttributesDto
  var relationships: List<RelationDto>?
}

@JsonInclude(Include.NON_NULL)
data class RelationDto(
  val id: String,
  val type: RelationshipType,
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
}

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
}

fun List<String>.toRelationshipTypeSet(): Set<RelationshipType> =
  mapNotNullTo(HashSet()) { relation ->
    RelationshipType.values().firstOrNull { it.name == relation.uppercase() }
  }

fun List<String>.isIncluded(relation: RelationshipType) = toRelationshipTypeSet().contains(relation)

fun <T, R : EntityDto> Page<T>.toSuccessCollectionResponseDto(mapper: (T) -> R) =
  SuccessCollectionResponseDto(
    data = content.map(mapper),
    pagination = PaginationDto(
      currentPage = this.number,
      totalPages = totalPages,
      totalElements = totalElements
    ),
  )

fun <T> Page<T>.toPaginationDto() = PaginationDto(number, totalPages, totalElements)
