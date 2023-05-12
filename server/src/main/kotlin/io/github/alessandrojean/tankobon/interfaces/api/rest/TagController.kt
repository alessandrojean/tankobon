package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TagSearch
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.TagLifecycle
import io.github.alessandrojean.tankobon.infrastructure.jooq.UnpagedSorted
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionTag
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toPaginationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
import io.swagger.v3.oas.annotations.tags.Tag as SwaggerTag

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@SwaggerTag(name = "Tag", description = "Operations regarding tags")
class TagController(
  private val libraryRepository: LibraryRepository,
  private val tagRepository: TagRepository,
  private val tagLifecycle: TagLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/tags")
  @Operation(summary = "Get all tags", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllTags(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionTag> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<TagEntityDto> {
    val tagsPage = tagRepository.findAll(
      search = TagSearch(
        libraryIds = libraryIds,
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = page,
    )

    val tags = referenceExpansion.expand(
      entities = tagsPage.content.map { it.toDto() },
      relationsToExpand = includes,
    )

    return SuccessPaginatedCollectionResponseDto(tags, tagsPage.toPaginationDto())
  }

  @GetMapping("v1/libraries/{libraryId}/tags")
  @Operation(summary = "Get all tags from a library", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllTagsByLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(name = "unpaged", required = false) unpaged: Boolean = false,
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<TagEntityDto> {
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

    val tags = tagRepository.findAll(
      search = TagSearch(
        libraryIds = listOf(library.id),
        searchTerm = searchTerm,
        userId = principal.user.id,
      ),
      pageable = pageRequest,
    )

    return tags.toSuccessCollectionResponseDto { it.toDto() }
  }

  @GetMapping("v1/tags/{tagId}")
  @Operation(summary = "Get a tag by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneTag(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") tagId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionTag> = emptySet(),
  ): SuccessEntityResponseDto<TagEntityDto> {
    val tag = tagRepository.findByIdOrNull(tagId)
      ?: throw IdDoesNotExistException("Tag not found")

    val library = libraryRepository.findById(tag.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = tag.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/tags")
  @Operation(summary = "Create a new tag", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneTag(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    tag: TagCreationDto,
  ): SuccessEntityResponseDto<TagEntityDto> {
    val library = libraryRepository.findByIdOrNull(tag.library)
      ?: throw RelationIdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = tagLifecycle.addTag(
      Tag(
        name = tag.name,
        description = tag.description,
        libraryId = tag.library,
      )
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/tags/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a tag by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneTag(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") tagId: String
  ) {
    val existing = tagRepository.findByIdOrNull(tagId)
      ?: throw IdDoesNotExistException("Tag not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    tagLifecycle.deleteTag(existing)
  }

  @PutMapping("v1/tags/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a tag by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneTag(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") tagId: String,
    @Valid @RequestBody
    tag: TagUpdateDto
  ) {
    val existing = tagRepository.findByIdOrNull(tagId)
      ?: throw IdDoesNotExistException("Tag not found")

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      name = tag.name,
      description = tag.description,
    )

    tagLifecycle.updateTag(toUpdate)
  }
}
