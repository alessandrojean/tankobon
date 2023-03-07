package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.LibraryLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.isIncluded
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toAttributesDto
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
import org.springframework.security.access.prepost.PreAuthorize
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
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "libraries", description = "Operations regarding libraries")
class LibraryController(
  private val libraryRepository: LibraryRepository,
  private val libraryLifecycle: LibraryLifecycle,
  private val userRepository: TankobonUserRepository,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("api/v1/libraries")
  @Operation(summary = "Get all libraries the user has access")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(required = false) ownerId: String?,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val libraries = when {
      principal.user.isAdmin && ownerId.isNullOrEmpty() ->
        libraryRepository.findAll()
      principal.user.isAdmin && !ownerId.isNullOrEmpty() ->
        libraryRepository.findByOwnerId(ownerId)
      !principal.user.isAdmin && ownerId == principal.user.id ->
        libraryRepository.findByOwnerId(ownerId)
      else -> libraryRepository.findByOwnerIdIncludingShared(principal.user.id)
    }

    val dtos = libraries
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }
      .let { referenceExpansion.expand(it, includes.toRelationshipTypeSet()) }

    return SuccessCollectionResponseDto(dtos)
  }

  @GetMapping("api/v1/users/{userId}/libraries")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #userId")
  @Operation(summary = "Get all libraries owned by a user by its id")
  fun getByOwnerId(
    @PathVariable userId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val userAttributes = user.toAttributesDto()
      .takeIf { includes.isIncluded(RelationshipType.OWNER) }

    val libraries = libraryRepository.findByOwnerId(user.id)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto(userAttributes) }

    return SuccessCollectionResponseDto(libraries)
  }

  @GetMapping("api/v1/libraries/{libraryId}")
  @Operation(summary = "Get a library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The library exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The library exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val libraryDto = referenceExpansion.expand(
      entity = library.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(libraryDto)
  }

  @PostMapping("api/v1/libraries")
  @Operation(summary = "Create a new library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The library was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "The owner user does not exist",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a library for other user with a non-admin user",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    library: LibraryCreationDto
  ): ResponseDto {
    val ownerId = library.owner ?: principal.user.id
    val owner = userRepository.findByIdOrNull(ownerId)
      ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    if (owner.id == library.owner && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = libraryLifecycle.addLibrary(
        Library(
          name = library.name,
          description = library.description,
          ownerId = owner.id
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("api/v1/libraries/{libraryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The library was deleted with success",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete other user library with a non-admin user",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String
  ) {
    val existing = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (existing.ownerId != principal.user.id && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      libraryLifecycle.deleteLibrary(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("api/v1/libraries/{libraryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The library was modified with success",
    ),
    ApiResponse(
      responseCode = "400",
      description = "The owner user does not exist",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to change the library owner with a non-admin user",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @Valid @RequestBody
    library: LibraryUpdateDto
  ) {
    val existing = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (existing.ownerId != library.owner && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    if (existing.ownerId != library.owner && userRepository.findByIdOrNull(library.owner) == null) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    }

    val toUpdate = existing.copy(
      name = library.name,
      description = library.description,
      ownerId = library.owner,
      sharedUsersIds = library.sharedUsers,
    )

    try {
      libraryLifecycle.updateLibrary(toUpdate)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
