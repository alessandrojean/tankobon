package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.LibraryLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryUpdateDto
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
) {

  @GetMapping("api/v1/libraries")
  @Operation(summary = "Get all libraries the user has access")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(required = false) ownerId: String?,
  ): List<LibraryDto> {
    val libraries = when {
      principal.user.isAdmin && ownerId.isNullOrEmpty() ->
        libraryRepository.findAll()
      principal.user.isAdmin && !ownerId.isNullOrEmpty() ->
        libraryRepository.findByOwnerId(ownerId)
      !principal.user.isAdmin && ownerId == principal.user.id ->
        libraryRepository.findByOwnerId(ownerId)
      else -> libraryRepository.findByOwnerIdIncludingShared(principal.user.id)
    }

    return libraries
      .sortedBy { it.name.lowercase() }
      .map { it.toDto(showSharedUsersIds = false) }
  }

  @GetMapping("api/v1/users/{userId}/libraries")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #userId")
  @Operation(summary = "Get all libraries owned by a user by its id")
  fun getByOwnerId(@PathVariable userId: String): List<LibraryDto> {
    val existingUser = userRepository.findByIdOrNull(userId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

    return libraryRepository.findByOwnerId(existingUser.id)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto(showSharedUsersIds = false) }
  }

  @GetMapping("api/v1/libraries/{libraryId}")
  @Operation(summary = "Get a library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The library exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
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
  ): LibraryDto {
    return libraryRepository.findByIdOrNull(libraryId)?.let {
      if (!principal.user.canAccessLibrary(it)) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }

      it.toDto(showSharedUsersIds = true)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("api/v1/libraries")
  @Operation(summary = "Create a new library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The library was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "The owner user does not exist",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a library for other user with a non-admin user",
      content = [Content()]
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    library: LibraryCreationDto
  ): LibraryDto {
    val ownerId = library.ownerId ?: principal.user.id
    val owner = userRepository.findByIdOrNull(ownerId)
      ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The owner does not exist")

    if (owner.id == library.ownerId && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create libraries for other users")
    }

    return try {
      libraryLifecycle
        .addLibrary(
          Library(
            name = library.name,
            description = library.description,
            ownerId = owner.id
          )
        )
        .toDto(showSharedUsersIds = false)
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
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete other user library with a non-admin user",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
      content = [Content()]
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String
  ) {
    val existing = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (existing.ownerId != principal.user.id && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete libraries from other users")
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
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "The owner user does not exist",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to change the library owner with a non-admin user",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
      content = [Content()]
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

    if (existing.ownerId != library.ownerId && !principal.user.isAdmin) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can change the owner of a library")
    }

    if (existing.ownerId != library.ownerId && userRepository.findByIdOrNull(library.ownerId) == null) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The owner user does not exist")
    }

    val toUpdate = existing.copy(
      name = library.name,
      description = library.description,
      ownerId = library.ownerId,
      sharedUsersIds = library.sharedUsersIds,
    )

    try {
      libraryLifecycle.updateLibrary(toUpdate)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
