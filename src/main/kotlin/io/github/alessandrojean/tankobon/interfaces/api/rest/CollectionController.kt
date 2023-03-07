package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.CollectionSearch
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.CollectionLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.CollectionCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.CollectionUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
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
@Tag(name = "collections", description = "Operations regarding collections")
class CollectionController(
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: CollectionRepository,
  private val collectionLifecycle: CollectionLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/collections")
  @Operation(summary = "Get all collections")
  fun getAllCollections(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false) libraryIds: List<String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val collectionsPage = collectionRepository.findAll(
      search = CollectionSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val collections = referenceExpansion.expand(
      collectionsPage.content.map { it.toDto() },
      includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(collections, collectionsPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/collections")
  @Operation(summary = "Get all collections from a library by its id")
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
  @Operation(summary = "Get a collection by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The collection exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The collection exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The collection does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable collectionId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val collection = collectionRepository.findByIdOrNull(collectionId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(collection.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = collection.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/collections")
  @Operation(summary = "Create a new collection")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The collection was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A collection with this name already exists in the library specified",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a collection for a library the user does not have access",
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
    @Valid @RequestBody
    collection: CollectionCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(collection.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = collectionLifecycle.addCollection(
        Collection(
          name = collection.name,
          description = collection.description,
          libraryId = collection.library
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/collections/{collectionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing collection by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The collection was deleted with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a collection from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The collection does not exist",
      content = [Content()]
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable collectionId: String
  ) {
    val existing = collectionRepository.findByIdOrNull(collectionId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      collectionLifecycle.deleteCollection(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/collections/{collectionId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing collection by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The collection was modified with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A collection with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a collection from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The collection does not exist",
      content = [Content()]
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable collectionId: String,
    @Valid @RequestBody
    collection: CollectionUpdateDto
  ) {
    val existing = collectionRepository.findByIdOrNull(collectionId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = collection.name,
      description = collection.description,
    )

    try {
      collectionLifecycle.updateCollection(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
