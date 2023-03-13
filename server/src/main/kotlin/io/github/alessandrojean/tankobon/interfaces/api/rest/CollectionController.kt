package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.CollectionSearch
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.CollectionLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.CollectionCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.CollectionEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.CollectionUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
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
@Tag(name = "Collection", description = "Operations regarding collections")
class CollectionController(
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: CollectionRepository,
  private val collectionLifecycle: CollectionLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/collections")
  @Operation(summary = "Get all collections", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllCollections(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<CollectionEntityDto> {
    val collectionsPage = collectionRepository.findAll(
      search = CollectionSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val collections = referenceExpansion.expand(
      entities = collectionsPage.content.map { it.toDto() },
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(collections, collectionsPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/collections")
  @Operation(summary = "Get all collections from a library", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllCollectionsByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<CollectionEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val collections = collectionRepository.findAll(
      search = CollectionSearch(
        libraryIds = listOf(library.id),
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    return collections.toSuccessCollectionResponseDto { it.toDto() }
  }

  @GetMapping("v1/collections/{collectionId}")
  @Operation(summary = "Get a collection by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneCollection(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") collectionId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<CollectionEntityDto> {
    val collection = collectionRepository.findByIdOrNull(collectionId)
      ?: throw IdDoesNotExistException("Collection not found")

    val library = libraryRepository.findById(collection.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = collection.toDto(),
      relationsToExpand = includes
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/collections")
  @Operation(summary = "Create a new collection", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneCollection(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    collection: CollectionCreationDto,
  ): SuccessEntityResponseDto<CollectionEntityDto> {
    val library = libraryRepository.findByIdOrNull(collection.library)
      ?: throw RelationIdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = collectionLifecycle.addCollection(
      Collection(
        name = collection.name,
        description = collection.description,
        libraryId = collection.library
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/collections/{collectionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a collection by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneCollection(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") collectionId: String
  ) {
    val existing = collectionRepository.findByIdOrNull(collectionId)
      ?: throw IdDoesNotExistException("Collection not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    collectionLifecycle.deleteCollection(existing)
  }

  @PutMapping("v1/collections/{collectionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a collection by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneCollection(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") collectionId: String,
    @Valid @RequestBody
    collection: CollectionUpdateDto
  ) {
    val existing = collectionRepository.findByIdOrNull(collectionId)
      ?: throw IdDoesNotExistException("Collection not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = collection.name,
      description = collection.description,
    )

    collectionLifecycle.updateCollection(toUpdate)
  }
}
