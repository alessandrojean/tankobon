package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.model.LibraryOwnerChangedException
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.LibraryLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(
  name = "Library",
  description = """
Libraries can store a collection of items related to the books, such as
publishers, people, collections, series, and tags. Each library belongs
to a single user, but can be shared with others if the user wants to.

The items created inside a library can't be accessed and used by books
in other libraries as each library is meant by design to be independent.
  """
)
class LibraryController(
  private val libraryRepository: LibraryRepository,
  private val libraryLifecycle: LibraryLifecycle,
  private val userRepository: TankobonUserRepository,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("api/v1/libraries")
  @Operation(summary = "Get all libraries the user has access", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllLibraries(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(required = false) @UUID(version = [4]) @Schema(format = "uuid") ownerId: String?,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessCollectionResponseDto<LibraryEntityDto> {
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
      .let { referenceExpansion.expand(it, includes) }

    return SuccessCollectionResponseDto(dtos)
  }

  @GetMapping("api/v1/users/{userId}/libraries")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or authentication.principal.user.id == #userId")
  @Operation(
    summary = "Get all libraries owned by a user by its id",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getAllLibrariesByOwner(
    @PathVariable("userId") @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @RequestParam(required = false, defaultValue = "false") includeShared: Boolean = false,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessCollectionResponseDto<LibraryEntityDto> {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val libraries = if (includeShared) {
      libraryRepository.findByOwnerIdIncludingShared(user.id)
    } else {
      libraryRepository.findByOwnerId(user.id)
    }

    val librariesDto = libraries
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = librariesDto,
      relationsToExpand = includes,
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("api/v1/libraries/{libraryId}")
  @Operation(summary = "Get a library by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<LibraryEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val libraryDto = referenceExpansion.expand(
      entity = library.toDto(),
      relationsToExpand = includes
    )

    return SuccessEntityResponseDto(libraryDto)
  }

  @PostMapping("api/v1/libraries")
  @Operation(summary = "Create a new library", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    library: LibraryCreationDto
  ): SuccessEntityResponseDto<LibraryEntityDto> {
    val ownerId = library.owner ?: principal.user.id
    val owner = userRepository.findByIdOrNull(ownerId)
      ?: throw RelationIdDoesNotExistException("User not found")

    if (owner.id != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val created = libraryLifecycle.addLibrary(
      Library(
        name = library.name,
        description = library.description,
        ownerId = owner.id
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("api/v1/libraries/{libraryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a library by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String
  ) {
    val existing = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (existing.ownerId != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    libraryLifecycle.deleteLibrary(existing)
  }

  @PutMapping("api/v1/libraries/{libraryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a library by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @Valid @RequestBody
    library: LibraryUpdateDto
  ) {
    val existing = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (library.owner != null && existing.ownerId != library.owner && !principal.user.isAdmin) {
      throw LibraryOwnerChangedException()
    }

    if (library.owner != null && userRepository.findByIdOrNull(library.owner) == null) {
      throw RelationIdDoesNotExistException("User not found")
    }

    val toUpdate = existing.copy(
      name = library.name,
      description = library.description,
      ownerId = library.owner ?: existing.ownerId,
      sharedUsersIds = library.sharedUsers,
    )

    libraryLifecycle.updateLibrary(toUpdate)
  }
}
