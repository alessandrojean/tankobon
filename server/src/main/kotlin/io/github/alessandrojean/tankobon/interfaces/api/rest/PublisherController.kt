package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.PublisherLinks
import io.github.alessandrojean.tankobon.domain.model.PublisherSearch
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.service.PublisherLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.image.PublisherPictureLifecycle
import io.github.alessandrojean.tankobon.infrastructure.jooq.UnpagedSorted
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.infrastructure.validation.SupportedImageFormat
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionPublisher
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toPaginationDto
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
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Publisher", description = "Operations regarding publishers")
class PublisherController(
  private val libraryRepository: LibraryRepository,
  private val publisherRepository: PublisherRepository,
  private val publisherLifecycle: PublisherLifecycle,
  private val referenceExpansion: ReferenceExpansion,
  private val publisherPictureLifecycle: PublisherPictureLifecycle,
) {

  @GetMapping("v1/publishers")
  @Operation(summary = "Get all publishers", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllPublishers(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionPublisher> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<PublisherEntityDto> {
    val publishersPage = publisherRepository.findAll(
      search = PublisherSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val publishers = referenceExpansion.expand(
      entities = publishersPage.content.map { it.toDto() }.withPictureIfExists(),
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(publishers, publishersPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/publishers")
  @Operation(summary = "Get all publishers from a library", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllPublishersByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionPublisher> = emptySet(),
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<PublisherEntityDto> {
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

    val publishersPage = publisherRepository.findAll(
      search = PublisherSearch(
        libraryIds = listOf(library.id),
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = pageRequest,
    )

    val publishers = referenceExpansion.expand(
      entities = publishersPage.content.map { it.toDto() }.withPictureIfExists(),
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(publishers, publishersPage.toPaginationDto())
  }

  @GetMapping("v1/publishers/{publisherId}")
  @Operation(summary = "Get a publisher by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    publisherId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionPublisher> = emptySet(),
  ): SuccessEntityResponseDto<PublisherEntityDto> {
    val publisher = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(publisher.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = publisher.toDto().withPictureIfExists(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/publishers")
  @Operation(summary = "Create a new publisher", security = [SecurityRequirement(name = "Basic Auth")])
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
        links = PublisherLinks(
          website = publisher.links.website,
          store = publisher.links.store,
          twitter = publisher.links.twitter,
          instagram = publisher.links.instagram,
          facebook = publisher.links.facebook,
          youTube = publisher.links.youTube,
        ),
        legalName = publisher.legalName,
        location = publisher.location,
        foundingYear = publisher.foundingYear,
        dissolutionYear = publisher.dissolutionYear,
        libraryId = publisher.library,
      ),
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @PostMapping("v1/publishers/{publisherId}/picture", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Upload a picture to a publisher by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun uploadPersonPicture(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    publisherId: String,
    @RequestParam("picture") @SupportedImageFormat pictureFile: MultipartFile,
  ) {
    val libraryId = publisherRepository.getLibraryIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    publisherPictureLifecycle.createImage(publisherId, pictureFile.bytes)
  }

  @DeleteMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a publisher by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    publisherId: String,
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    publisherLifecycle.deletePublisher(existing)
  }

  @DeleteMapping("v1/publishers/{publisherId}/picture")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a publisher picture by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deletePersonPicture(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    publisherId: String,
  ) {
    val existing = publisherRepository.findByIdOrNull(publisherId)
      ?: throw IdDoesNotExistException("Publisher not found")

    val library = libraryRepository.findById(publisherRepository.getLibraryIdOrNull(existing.id)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    publisherPictureLifecycle.deleteImage(publisherId)
  }

  @PutMapping("v1/publishers/{publisherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a publisher by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOnePublisher(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    publisherId: String,
    @Valid @RequestBody
    publisher: PublisherUpdateDto,
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
      links = PublisherLinks(
        website = publisher.links.website,
        store = publisher.links.store,
        twitter = publisher.links.twitter,
        instagram = publisher.links.instagram,
        facebook = publisher.links.facebook,
        youTube = publisher.links.youTube,
      ),
      legalName = publisher.legalName,
      location = publisher.location,
      foundingYear = publisher.foundingYear,
      dissolutionYear = publisher.dissolutionYear,
    )

    publisherLifecycle.updatePublisher(toUpdate)
  }

  private fun PublisherEntityDto.withPictureIfExists(): PublisherEntityDto {
    if (!publisherPictureLifecycle.hasImage(id)) {
      return this
    }

    return copy(
      relationships = relationships.orEmpty() + listOf(RelationDto(id = id, type = ReferenceExpansionPublisher.PUBLISHER_PICTURE)),
    )
  }

  private fun List<PublisherEntityDto>.withPictureIfExists(): List<PublisherEntityDto> {
    val entitiesWithImages = publisherPictureLifecycle.getEntitiesWithImages(map { it.id })

    if (entitiesWithImages.isEmpty()) {
      return this
    }

    return map {
      it.copy(
        relationships = it.relationships.orEmpty() + listOfNotNull(
          RelationDto(id = it.id, type = ReferenceExpansionPublisher.PUBLISHER_PICTURE)
            .takeIf { relation -> entitiesWithImages.getOrDefault(relation.id, false) },
        ),
      )
    }
  }
}
