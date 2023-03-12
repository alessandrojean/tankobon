package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.SeriesLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SeriesCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SeriesEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SeriesUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toPaginationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
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

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Series", description = "Operations regarding series")
class SeriesController(
  private val libraryRepository: LibraryRepository,
  private val seriesRepository: SeriesRepository,
  private val seriesLifecycle: SeriesLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/series")
  @Operation(summary = "Get all series", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllSeries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<SeriesEntityDto> {
    val seriesPage = seriesRepository.findAll(
      search = SeriesSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val series = referenceExpansion.expand(
      entities = seriesPage.content.map { it.toDto() },
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(series, seriesPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/series")
  @Operation(summary = "Get all series from a library", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllSeriesByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<SeriesEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
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
  @Operation(summary = "Get a series by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneSeries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") seriesId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<SeriesEntityDto> {
    val series = seriesRepository.findByIdOrNull(seriesId)
      ?: throw IdDoesNotExistException("Series not found")

    val library = libraryRepository.findById(series.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = series.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/series")
  @Operation(summary = "Create a new series", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneSeries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    series: SeriesCreationDto,
  ): SuccessEntityResponseDto<SeriesEntityDto> {
    val library = libraryRepository.findByIdOrNull(series.library)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = seriesLifecycle.addSeries(
      Series(
        name = series.name,
        description = series.description,
        libraryId = series.library
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/series/{seriesId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a series by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneSeries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") seriesId: String
  ) {
    val existing = seriesRepository.findByIdOrNull(seriesId)
      ?: throw IdDoesNotExistException("Series not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    seriesLifecycle.deleteSeries(existing)
  }

  @PutMapping("v1/series/{seriesId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a series by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneSeries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") seriesId: String,
    @Valid @RequestBody
    series: SeriesUpdateDto
  ) {
    val existing = seriesRepository.findByIdOrNull(seriesId)
      ?: throw IdDoesNotExistException("Series not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = series.name,
      description = series.description,
    )

    seriesLifecycle.updateSeries(toUpdate)
  }
}
