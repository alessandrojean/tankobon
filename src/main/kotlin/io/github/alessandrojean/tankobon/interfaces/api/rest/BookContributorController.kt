package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.persistence.BookContributorRepository
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "people", description = "Operations regarding book contributors")
class BookContributorController(
  private val bookRepository: BookRepository,
  private val libraryRepository: LibraryRepository,
  private val bookContributorRepository: BookContributorRepository,
) {

  @GetMapping("v1/books/{bookId}/contributors")
  @Operation(summary = "Get all contributors from a book by its id")
  fun getAll(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable bookId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val libraryId = bookRepository.getLibraryIdOrNull(bookId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val contributors = bookContributorRepository.findAllAsDtoByBookId(bookId)

    return SuccessCollectionResponseDto(contributors)
  }

  @GetMapping("v1/contributors/{contributorId}")
  @Operation(summary = "Get a book contributor by its id")
  fun getOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable contributorId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val contributor = bookContributorRepository.findByIdAsDtoOrNull(contributorId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val libraryId = bookRepository.getLibraryIdOrNull(contributor.relationships!!.first().id)!!
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return SuccessEntityResponseDto(contributor)
  }

}
