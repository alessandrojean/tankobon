package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.ContributorRoleSearch
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.ContributorRoleLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.jooq.UnpagedSorted
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ContributorRoleCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ContributorRoleEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ContributorRoleUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionContributorRole
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
@Tag(name = "ContributorRole", description = "Operations regarding contributor roles")
class ContributorRoleController(
  private val libraryRepository: LibraryRepository,
  private val contributorRoleRepository: ContributorRoleRepository,
  private val contributorRoleLifecycle: ContributorRoleLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/contributor-roles")
  @Operation(summary = "Get all contributor roles", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllContributorRoles(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionContributorRole> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<ContributorRoleEntityDto> {
    val contributorRolesPage = contributorRoleRepository.findAll(
      search = ContributorRoleSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val contributorRoles = referenceExpansion.expand(
      entities = contributorRolesPage.content.map { it.toDto() },
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(contributorRoles, contributorRolesPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/contributor-roles")
  @Operation(
    summary = "Get all contributor roles from a library",
    security = [SecurityRequirement(name = "Basic Auth")],
  )
  fun getAllContributorRolesByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    libraryId: String,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<ContributorRoleEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val sort = when {
      page.sort.isSorted -> page.sort
      !searchTerm.isNullOrBlank() -> Sort.by("relevance")
      else -> Sort.unsorted()
    }

    val pageRequest = if (unpaged) {
      UnpagedSorted(sort)
    } else {
      PageRequest.of(page.pageNumber, page.pageSize, sort)
    }

    val contributorRoles = contributorRoleRepository.findAll(
      search = ContributorRoleSearch(
        libraryIds = listOf(library.id),
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = pageRequest,
    )

    return contributorRoles.toSuccessCollectionResponseDto { it.toDto() }
  }

  @GetMapping("v1/contributor-roles/{contributorRoleId}")
  @Operation(summary = "Get a contributor role by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneContributorRole(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    contributorRoleId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionContributorRole> = emptySet(),
  ): SuccessEntityResponseDto<ContributorRoleEntityDto> {
    val contributorRole = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw IdDoesNotExistException("Contributor role not found")

    val library = libraryRepository.findById(contributorRole.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = contributorRole.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/contributor-roles")
  @Operation(summary = "Create a new contributor role", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneContributorRole(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    contributorRole: ContributorRoleCreationDto,
  ): SuccessEntityResponseDto<ContributorRoleEntityDto> {
    val library = libraryRepository.findByIdOrNull(contributorRole.library)
      ?: throw RelationIdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = contributorRoleLifecycle.addContributorRole(
      ContributorRole(
        name = contributorRole.name,
        description = contributorRole.description,
        libraryId = contributorRole.library,
      ),
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/contributor-roles/{contributorRoleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a contributor role by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneContributorRole(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    contributorRoleId: String,
  ) {
    val existing = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw IdDoesNotExistException("Contributor role not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    contributorRoleLifecycle.deleteContributorRole(existing)
  }

  @PutMapping("v1/contributor-roles/{contributorRoleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a contributor role by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneContributorRole(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    contributorRoleId: String,
    @Valid @RequestBody
    contributorRole: ContributorRoleUpdateDto,
  ) {
    val existing = contributorRoleRepository.findByIdOrNull(contributorRoleId)
      ?: throw IdDoesNotExistException("Contributor role not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = contributorRole.name,
      description = contributorRole.description,
    )

    contributorRoleLifecycle.updateContributorRole(toUpdate)
  }
}
