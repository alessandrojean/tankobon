package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.SeriesLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SeriesCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SeriesUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toPaginationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toRelationshipTypeSet
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "series", description = "Operations regarding series")
class SeriesController(
  private val libraryRepository: LibraryRepository,
  private val seriesRepository: SeriesRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/series")
  @Operation(summary = "Get all series")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false) libraryIds: List<String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val seriesPage = seriesRepository.findAll(
      search = SeriesSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val series = referenceExpansion.expand(
      seriesPage.content.map { it.toDto() },
      includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(series, seriesPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/series")
  @Operation(summary = "Get all series from a library by its id")
  fun getAllCollections(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable libraryId: String,
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val collections = seriesRepository.findAll(
      search = SeriesSearch(
        libraryIds = listOf(library.id),
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    return collections.toSuccessCollectionResponseDto { it.toDto() }
  }

  @GetMapping("v1/series/{seriesId}")
  @Operation(summary = "Get a series by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The series exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The series exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The series does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable seriesId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val series = seriesRepository.findByIdOrNull(seriesId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(series.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = series.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/series")
  @Operation(summary = "Create a new series in a library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The series was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A series with this name already exists in the library specified",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a series for a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    series: SeriesCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(series.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = seriesLifecycle.addSeries(
        Series(
          name = series.name,
          description = series.description,
          libraryId = series.library
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/series/{seriesId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing series by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The series was deleted with success",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a series from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The series does not exist",
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable seriesId: String
  ) {
    val existing = seriesRepository.findByIdOrNull(seriesId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      seriesLifecycle.deleteSeries(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/series/{seriesId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing series by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The series was modified with success",
    ),
    ApiResponse(
      responseCode = "400",
      description = "A series with this name already exists",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a series from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The series does not exist",
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable seriesId: String,
    @Valid @RequestBody
    series: SeriesUpdateDto
  ) {
    val existing = seriesRepository.findByIdOrNull(seriesId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = series.name,
      description = series.description,
    )

    try {
      seriesLifecycle.updateSeries(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
