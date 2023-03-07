package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.ContributorRoleLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ContributorRoleCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ContributorRoleUpdateDto
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
@Tag(name = "contributor-roles", description = "Operations regarding contributor roles")
class ContributorRoleController(
  private val libraryRepository: LibraryRepository,
  private val contributorRoleRepository: ContributorRoleRepository,
  private val contributorRoleLifecycle: ContributorRoleLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/contributor-roles")
  @Operation(summary = "Get all contributor roles from a library by its id")
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

    val roles = contributorRoleRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = roles,
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/contributor-roles/{contributorRoleId}")
  @Operation(summary = "Get a contributor role by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The contributor role exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The contributor role exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The contributor role does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable contributorRoleId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val contributorRole = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(contributorRole.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = contributorRole.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/contributor-roles")
  @Operation(summary = "Create a new contributor role")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The contributor role was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A contributor role with this name already exists in the library specified",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a contributor role for a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    contributorRole: ContributorRoleCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(contributorRole.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = contributorRoleLifecycle.addContributorRole(
        ContributorRole(
          name = contributorRole.name,
          description = contributorRole.description,
          libraryId = contributorRole.library
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/contributor-roles/{contributorRoleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing contributor role by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The contributor role was deleted with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a contributor role from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The contributor role does not exist",
      content = [Content()]
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable contributorRoleId: String
  ) {
    val existing = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      contributorRoleLifecycle.deleteContributorRole(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/contributor-roles/{contributorRoleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing contributor role by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The contributor role was modified with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A contributor role with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a contributor role from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The contributor role does not exist",
      content = [Content()]
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable contributorRoleId: String,
    @Valid @RequestBody
    contributorRole: ContributorRoleUpdateDto
  ) {
    val existing = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = contributorRole.name,
      description = contributorRole.description,
    )

    try {
      contributorRoleLifecycle.updateContributorRole(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
