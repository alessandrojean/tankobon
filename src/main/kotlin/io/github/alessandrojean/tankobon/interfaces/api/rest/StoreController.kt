package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.service.StoreLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreUpdateDto
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
@Tag(name = "stores", description = "Operations regarding stores")
class StoreController(
  private val libraryRepository: LibraryRepository,
  private val storeRepository: StoreRepository,
  private val storeLifecycle: StoreLifecycle,
) {

  @GetMapping("v1/libraries/{libraryId}/stores")
  @Operation(summary = "Get all stores from a library by its id")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
  ): List<StoreDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The library does not exist")

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "The user does not have access to the library requested")
    }

    return storeRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }
  }

  @GetMapping("v1/stores/{storeId}")
  @Operation(summary = "Get a store by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The store exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The store exists and the user doesn't have access to it",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The store does not exist",
      content = [Content()]
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable storeId: String,
  ): StoreDto {
    return storeRepository.findByIdOrNull(storeId)?.let {
      val library = libraryRepository.findById(it.libraryId)

      if (!principal.user.canAccessLibrary(library)) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
      }

      it.toDto()
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PostMapping("v1/libraries/{libraryId}/stores")
  @Operation(summary = "Create a new store in a library")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The store was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = LibraryDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A store with this name already exists in the library specified",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a store for a library the user does not have access",
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
    store: StoreCreationDto,
  ): StoreDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The library does not exist")

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, "The user does not have access to the requested library")
    }

    return try {
      storeLifecycle
        .addStore(
          Store(
            name = store.name,
            description = store.description,
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

  @DeleteMapping("v1/stores/{storeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing store by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The store was deleted with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a store from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The store does not exist",
      content = [Content()]
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable storeId: String
  ) {
    val existing = storeRepository.findByIdOrNull(storeId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      storeLifecycle.deleteStore(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/stores/{storeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing store by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The store was modified with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A store with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a store from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The store does not exist",
      content = [Content()]
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable storeId: String,
    @Valid @RequestBody
    store: StoreUpdateDto
  ) {
    val existing = storeRepository.findByIdOrNull(storeId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = store.name,
      description = store.description,
    )

    try {
      storeLifecycle.updateStore(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
