package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.service.PublisherLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
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
) {

  @GetMapping("v1/libraries/{libraryId}/publishers")
  @Operation(summary = "Get all publishers from a library by its id")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
  ): List<PublisherDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The library does not exist")

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "The user does not have access to the library requested")
    }

    return publisherRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }
  }

  @GetMapping("v1/publishers/{publisherId}")
  @Operation(summary = "Get a publisher by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The publisher exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The publisher exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable publisherId: String,
  ): PublisherDto {
    return publisherRepository.findByIdOrNull(publisherId)?.let {
      val library = libraryRepository.findById(it.libraryId)

      if (!principal.user.canAccessLibrary(library)) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }

      it.toDto()
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("v1/libraries/{libraryId}/publishers")
  @Operation(summary = "Create a new publisher in a library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The publisher was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A publisher with this name already exists in the library specified",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a publisher for a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
      content = [Content()]
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @Valid @RequestBody
    publisher: PublisherCreationDto,
  ): PublisherDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The library does not exist")

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "The user does not have access to the requested library")
    }

    return try {
      publisherLifecycle
        .addPublisher(
          Publisher(
            name = publisher.name,
            description = publisher.description,
            libraryId = libraryId
          )
        )
        .toDto()
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
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a publisher from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
      content = [Content()]
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
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A publisher with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a publisher from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The publisher does not exist",
      content = [Content()]
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
