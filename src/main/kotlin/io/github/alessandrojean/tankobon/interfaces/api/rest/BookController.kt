package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.BookSearch
import io.github.alessandrojean.tankobon.domain.model.DuplicateCodeException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIsNotFromSameLibraryException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.BookLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
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
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
@Tag(name = "books", description = "Operations regarding books")
class BookController(
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val libraryRepository: LibraryRepository,
  private val referenceExpansion: ReferenceExpansion,
) {

  @PageableAsQueryParam
  @GetMapping("v1/books")
  @Operation(summary = "Get all books the user has access")
  fun getAllBooks(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false) libraryIds: List<String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val sort = when {
      page.sort.isSorted -> page.sort
      !searchTerm.isNullOrBlank() -> Sort.by("relevance")
      else -> Sort.unsorted()
    }

    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      sort,
    )

    val bookSearch = BookSearch(
      libraryIds = libraryIds,
      searchTerm = searchTerm,
      userId = principal.user.id,
    )

    val booksPage = bookDtoRepository.findAll(bookSearch, pageRequest)
    val books = referenceExpansion.expand(booksPage.content, includes.toRelationshipTypeSet())

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/libraries/{libraryId}/books")
  @PageableAsQueryParam
  @Operation(summary = "Get all books from a library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The operation succeeded",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class)),
      ],
    ),
    ApiResponse(
      responseCode = "403",
      description = "The user does not have access to the library specified"
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist"
    )
  )
  fun getAllBooksFromLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val sort = when {
      page.sort.isSorted -> page.sort
      else -> Sort.unsorted()
    }

    val bookSearch = BookSearch(listOf(libraryId), userId = principal.user.id)
    val pageRequest = PageRequest.of(page.pageNumber, page.pageSize, sort)
    val booksPage = bookDtoRepository.findAll(bookSearch, pageRequest)
    val books = referenceExpansion.expand(booksPage.content, includes.toRelationshipTypeSet())

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/libraries/{libraryId}/books/latest")
  @Operation(summary = "Get newly added or updated books from a library by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The operation succeeded",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class)),
      ],
    ),
    ApiResponse(
      responseCode = "403",
      description = "The user does not have access to the library specified"
    ),
    ApiResponse(
      responseCode = "404",
      description = "The library does not exist"
    ),
  )
  fun getLatestBooksInLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
    @Parameter(hidden = true) page: Pageable,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val sort = Sort.by(Sort.Order.desc("modifiedAt"))
    val bookSearch = BookSearch(listOf(libraryId), userId = principal.user.id)
    val pageRequest = PageRequest.of(page.pageNumber, page.pageSize, sort)
    val booksPage = bookDtoRepository.findAll(bookSearch, pageRequest)
    val books = referenceExpansion.expand(booksPage.content, includes.toRelationshipTypeSet())

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/books/{bookId}")
  @Operation(summary = "Get a book by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "200",
      description = "The book exists and the user has access to it",
      content = [
        Content(mediaType = "application/json", schema = Schema(implementation = ResponseDto::class)),
      ],
    ),
    ApiResponse(
      responseCode = "403",
      description = "The book exists and the user does not have access to it"
    ),
    ApiResponse(
      responseCode = "404",
      description = "The book does not exist"
    )
  )
  fun getOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable bookId: String,
    @RequestParam(required = false, defaultValue = "") includes: List<String> = emptyList(),
  ): ResponseDto {
    val book = bookDtoRepository.findByIdOrNull(bookId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(bookRepository.getLibraryIdOrNull(bookId)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    val dto = referenceExpansion.expand(book, includes.toRelationshipTypeSet())

    return SuccessEntityResponseDto(dto)
  }

  @PostMapping("v1/books")
  @Operation(summary = "Create a new book")
  fun createOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable libraryId: String,
    @Valid @RequestBody
    book: BookCreationDto,
  ): ResponseDto {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    return try {
      val created = bookLifecycle.addBook(book, principal.user)

      SuccessEntityResponseDto(created)
    } catch (e: RelationIdDoesNotExistException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: RelationIsNotFromSameLibraryException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: DuplicateCodeException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: UserDoesNotHaveAccessException) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @PutMapping("v1/books/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify an existing book by its id")
  fun updateOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable bookId: String,
    @Valid @RequestBody
    book: BookUpdateDto,
  ) {
    val libraryId = bookRepository.getLibraryIdOrNull(bookId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      bookDtoRepository.update(bookId, book, principal.user)
    } catch (e: RelationIdDoesNotExistException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: RelationIsNotFromSameLibraryException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: DuplicateCodeException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
    } catch (e: UserDoesNotHaveAccessException) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }

  @DeleteMapping("v1/books/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete an existing book by its id")
  @ApiResponses(
    ApiResponse(
      responseCode = "204",
      description = "The book was deleted with success",
    ),
    ApiResponse(
      responseCode = "403",
      description = "Attempted to delete a book from a library the user does not have access",
    ),
    ApiResponse(
      responseCode = "404",
      description = "The book does not exist",
    ),
  )
  fun deleteOne(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable bookId: String
  ) {
    val existing = bookRepository.findByIdOrNull(bookId)
      ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    val library = libraryRepository.findById(bookRepository.getLibraryIdOrNull(existing.id)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    try {
      bookLifecycle.deleteBook(existing)
    } catch (e: Exception) {
      throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
    }
  }
}