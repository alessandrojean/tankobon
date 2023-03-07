package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.service.PersonLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PersonCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PersonUpdateDto
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
@Tag(name = "people", description = "Operations regarding people")
class PersonController(
  private val libraryRepository: LibraryRepository,
  private val personRepository: PersonRepository,
  private val personLifecycle: PersonLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/libraries/{libraryId}/people")
  @Operation(summary = "Get all people from a library by its id")
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

    val people = personRepository
      .findByLibraryId(libraryId)
      .sortedBy { it.name.lowercase() }
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = people,
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/people/{personId}")
  @Operation(summary = "Get a person by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The person exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "403",
      description = "The person exists and the user doesn't have access to it",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The person does not exist",
    ),
  )
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable personId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val person = personRepository.findByIdOrNull(personId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(person.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val expanded = referenceExpansion.expand(
      entity = person.toDto(),
      relationsToExpand = includes.toRelationshipTypeSet()
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/people")
  @Operation(summary = "Create a new person")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The person was created with success",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class))
      ]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A person with this name already exists in the library specified",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to create a person for a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist",
    ),
  )
  fun addOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    person: PersonCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(person.library)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = personLifecycle.addPerson(
        Person(
          name = person.name,
          description = person.description,
          libraryId = person.library,
        )
      )

      SuccessEntityResponseDto(created.toDto())
    } catch (e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/people/{personId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing person by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The person was deleted with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a person from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The person does not exist",
      content = [Content()]
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable personId: String
  ) {
    val existing = personRepository.findByIdOrNull(personId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      personLifecycle.deletePerson(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/people/{personId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing person by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The person was modified with success",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "400",
      description = "A person with this name already exists",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to modify a person from a library the user does not have access",
      content = [Content()]
    ),
    ApiResponse(
      responseCode = "404",
      description = "The person does not exist",
      content = [Content()]
    ),
  )
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable personId: String,
    @Valid @RequestBody
    person: PersonUpdateDto
  ) {
    val existing = personRepository.findByIdOrNull(personId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(existing.libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val toUpdate = existing.copy(
      name = person.name,
      description = person.description,
    )

    try {
      personLifecycle.updatePerson(toUpdate)
    } catch(e: DuplicateNameException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}
