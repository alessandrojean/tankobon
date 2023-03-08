package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.service.PublisherLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
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
@Tag(name = "Publisher", description = "Operations regarding publishers")
class PublisherController(
  private val libraryRepository: LibraryRepository,
  private val publisherRepository: PublisherRepository,
  private val publisherLifecycle: PublisherLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/publishers")
  @Operation(summary = "Get all publishers from a library")
  fun getAllPublishersByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessCollectionResponseDto<PublisherEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val publishers = publisherRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = publishers,
      relationsToExpand = includes,
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/publishers/{publisherId}")
  @Operation(summary = "Get a publisher by its id")
  fun getOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") publisherId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<PublisherEntityDto> {
    val publisher = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(publisher.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = publisher.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/publishers")
  @Operation(summary = "Create a new publisher")
  fun addOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    publisher: PublisherCreationDto,
  ): SuccessEntityResponseDto<PublisherEntityDto> {
    val library = libraryRepository.findByIdOrNull(publisher.library)
      ?: throw RelationIdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = publisherLifecycle.addPublisher(
      Publisher(
        name = publisher.name,
        description = publisher.description,
        libraryId = publisher.library
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a publisher by its id")
  fun deleteOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") publisherId: String
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    publisherLifecycle.deletePublisher(existing)
  }

  @PutMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a publisher by its id")
  fun updateOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") publisherId: String,
    @Valid @RequestBody
    publisher: PublisherUpdateDto
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = publisher.name,
      description = publisher.description,
    )

    publisherLifecycle.updatePublisher(toUpdate)
  }
}
