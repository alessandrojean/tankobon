package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.service.PublisherLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toRelationshipTypeSet
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
@Tag(name = "publishers", description = "Operations regarding publishers")
class PublisherController(
  private val libraryRepository: LibraryRepository,
  private val publisherRepository: PublisherRepository,
  private val publisherLifecycle: PublisherLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/publishers")
  @Operation(summary = "Get all publishers from a library by its id")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val publishers = publisherRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = publishers,
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/publishers/{publisherId}")
  @Operation(summary = "Get a publisher by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The publisher exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The publisher exists and the user doesn't have access to it",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable publisherId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val publisher = publisherRepository.findByIdOrNull(publisherId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(publisher.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = publisher.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/publishers")
  @Operation(summary = "Create a new publisher in a library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The publisher was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A publisher with this name already exists in the library specified",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a publisher for a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    publisher: PublisherCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(publisher.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = publisherLifecycle.addPublisher(
        Publisher(
          name = publisher.name,
          description = publisher.description,
          libraryId = publisher.library
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing publisher by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The publisher was deleted with success",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a publisher from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable publisherId: String
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      publisherLifecycle.deletePublisher(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing publisher by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The publisher was modified with success",
    ),
    ApiResponse(
      responseCode = "400",
      description = "A publisher with this name already exists",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a publisher from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable publisherId: String,
    @Valid @RequestBody
    publisher: PublisherUpdateDto
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = publisher.name,
      description = publisher.description,
    )

    try {
      publisherLifecycle.updatePublisher(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
