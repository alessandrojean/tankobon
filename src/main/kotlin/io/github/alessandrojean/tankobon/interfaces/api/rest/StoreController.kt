package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.StoreLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.StoreUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
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
@Tag(name = "Store", description = "Operations regarding stores")
class StoreController(
  private val libraryRepository: LibraryRepository,
  private val storeRepository: StoreRepository,
  private val storeLifecycle: StoreLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/stores")
  @Operation(summary = "Get all stores from a library")
  fun getAllStoresByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessCollectionResponseDto<StoreEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val stores = storeRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = stores,
      relationsToExpand = includes,
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/stores/{storeId}")
  @Operation(summary = "Get a store by its id")
  fun getOneStory(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") storeId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<StoreEntityDto> {
    val store = storeRepository.findByIdOrNull(storeId)
      ?: throw IdDoesNotExistException("Store not found")

    val library = libraryRepository.findById(store.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = store.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/stores")
  @Operation(summary = "Create a new store")
  fun addOneStore(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    store: StoreCreationDto,
  ): SuccessEntityResponseDto<StoreEntityDto> {
    val library = libraryRepository.findByIdOrNull(store.library)
      ?: throw RelationIdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = storeLifecycle.addStore(
      Store(
        name = store.name,
        description = store.description,
        libraryId = store.library
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/stores/{storeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a store by its id")
  fun deleteOneStore(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") storeId: String
  ) {
    val existing = storeRepository.findByIdOrNull(storeId)
      ?: throw IdDoesNotExistException("Store not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    storeLifecycle.deleteStore(existing)
  }

  @PutMapping("v1/stores/{storeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a store by its id")
  fun updateOneStore(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") storeId: String,
    @Valid @RequestBody
    store: StoreUpdateDto
  ) {
    val existing = storeRepository.findByIdOrNull(storeId)
      ?: throw IdDoesNotExistException("Store not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = store.name,
      description = store.description,
    )

    storeLifecycle.updateStore(toUpdate)
  }
}
