package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.BookContributorRepository
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookContributorEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionBookContributor
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.hibernate.validator.constraints.UUID
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "BookContributor", description = "Operations regarding book contributors")
class BookContributorController(
  private val bookRepository: BookRepository,
  private val libraryRepository: LibraryRepository,
  private val bookContributorRepository: BookContributorRepository,
  private val referenceExpansion: ReferenceExpansion,
) {

  @GetMapping("v1/books/{bookId}/contributors")
  @Operation(summary = "Get all contributors from a book", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllContributorsByBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionBookContributor> = emptySet(),
  ): SuccessCollectionResponseDto<BookContributorEntityDto> {
    val libraryId = bookRepository.getLibraryIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val contributors = bookContributorRepository.findAllAsDtoByBookId(bookId)

    val expanded = referenceExpansion.expand(
      entities = contributors,
      relationsToExpand = includes,
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/contributors/{contributorId}")
  @Operation(summary = "Get a book contributor by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneBookContributor(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") contributorId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionBookContributor> = emptySet(),
  ): SuccessEntityResponseDto<BookContributorEntityDto> {
    val contributor = bookContributorRepository.findByIdAsDtoOrNull(contributorId)
      ?: throw IdDoesNotExistException("Book contributor not found")

    val libraryId = bookRepository.getLibraryIdOrNull(contributor.relationships!!.first().id)!!
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = contributor,
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

}
