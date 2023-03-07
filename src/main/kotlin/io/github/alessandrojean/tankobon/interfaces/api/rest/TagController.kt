package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.TagLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toRelationshipTypeSet
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
import io.swagger.v3.oas.annotations.tags.Tag as SwaggerTag

@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@SwaggerTag(name = "tags", description = "Operations regarding tags")
class TagController(
  private val libraryRepository: LibraryRepository,
  private val tagRepository: TagRepository,
  private val tagLifecycle: TagLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/tags")
  @Operation(summary = "Get all tags from a library by its id")
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

    val tags = tagRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = tags,
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/tags/{tagId}")
  @Operation(summary = "Get a tag by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The tag exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The tag exists and the user doesn't have access to it",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The tag does not exist",
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable tagId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val tag = tagRepository.findByIdOrNull(tagId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(tag.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = tag.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/tags")
  @Operation(summary = "Create a new tag")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The tag was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A tag with this name already exists in the library specified",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a tag for a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    tag: TagCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(tag.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = tagLifecycle.addTag(
        Tag(
          name = tag.name,
          description = tag.description,
          libraryId = tag.library,
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/tags/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing tag by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The tag was deleted with success",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a tag from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The tag does not exist",
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable tagId: String
  ) {
    val existing = tagRepository.findByIdOrNull(tagId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      tagLifecycle.deleteTag(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/tags/{tagId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing tag by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The tag was modified with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A tag with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a tag from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The tag does not exist",
      content = [Content()]
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable tagId: String,
    @Valid @RequestBody
    tag: TagUpdateDto
  ) {
    val existing = tagRepository.findByIdOrNull(tagId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = tag.name,
      description = tag.description,
    )

    try {
      tagLifecycle.updateTag(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
