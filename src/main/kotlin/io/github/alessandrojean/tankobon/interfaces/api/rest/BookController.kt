package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.BookSearch
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.service.BookLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.image.BookCoverLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.PageImpl
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
@Tag(name = "Book", description = "Operations regarding books")
class BookController(
  private val bookLifecycle: BookLifecycle,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val libraryRepository: LibraryRepository,
  private val collectionRepository: CollectionRepository,
  private val bookCoverLifecycle: BookCoverLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @PageableAsQueryParam
  @GetMapping("v1/books")
  @Operation(summary = "Get all books", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllBooks(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = false) searchTerm: String? = null,
    @RequestParam(name = "libraries", required = false)
    @ArraySchema(schema = Schema(format = "uuid"))
    libraryIds: Set<@UUID(version = [4]) String>? = null,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<BookEntityDto> {
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
    val books = referenceExpansion.expand(booksPage.content, includes)

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/libraries/{libraryId}/books")
  @PageableAsQueryParam
  @Operation(summary = "Get all books from a library", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllBooksFromLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<BookEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val sort = when {
      page.sort.isSorted -> page.sort
      else -> Sort.unsorted()
    }

    val bookSearch = BookSearch(listOf(libraryId), userId = principal.user.id)
    val pageRequest = PageRequest.of(page.pageNumber, page.pageSize, sort)
    val booksPage = bookDtoRepository.findAll(bookSearch, pageRequest)
    val books = referenceExpansion.expand(booksPage.content, includes)

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/libraries/{libraryId}/books/latest")
  @Operation(
    summary = "Get newly added or updated books from a library",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getLatestBooksInLibrary(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") libraryId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<BookEntityDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val sort = Sort.by(Sort.Order.desc("modifiedAt"))
    val bookSearch = BookSearch(listOf(libraryId), userId = principal.user.id)
    val pageRequest = PageRequest.of(page.pageNumber, page.pageSize, sort)
    val booksPage = bookDtoRepository.findAll(bookSearch, pageRequest)
    val books = referenceExpansion.expand(booksPage.content, includes)

    return PageImpl(books, booksPage.pageable, booksPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/books/{bookId}")
  @Operation(summary = "Get a book by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<BookEntityDto> {
    val book = bookDtoRepository.findByIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")

    val library = libraryRepository.findById(bookRepository.getLibraryIdOrNull(bookId)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val dto = referenceExpansion.expand(book, includes)

    return SuccessEntityResponseDto(dto)
  }

  @PostMapping("v1/books")
  @Operation(summary = "Create a new book", security = [SecurityRequirement(name = "Basic Auth")])
  fun createOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    book: BookCreationDto,
  ): SuccessEntityResponseDto<BookEntityDto> {
    val libraryId = collectionRepository.getLibraryIdOrNull(book.collection)
      ?: throw RelationIdDoesNotExistException("Collection not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = bookLifecycle.addBook(book, principal.user)

    return SuccessEntityResponseDto(created)
  }

  @PostMapping("v1/books/{bookId}/cover", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Upload a cover to a book by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun uploadBookCover(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String,
    @RequestParam("cover") coverFile: MultipartFile,
  ) {
    val libraryId = bookRepository.getLibraryIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    bookCoverLifecycle.createCover(bookId, coverFile.bytes)
  }

  @PutMapping("v1/books/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a book by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String,
    @Valid @RequestBody
    book: BookUpdateDto,
  ) {
    val libraryId = bookRepository.getLibraryIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    bookDtoRepository.update(bookId, book, principal.user)
  }

  @DeleteMapping("v1/books/{bookId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a book by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String
  ) {
    val existing = bookRepository.findByIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")

    val library = libraryRepository.findById(bookRepository.getLibraryIdOrNull(existing.id)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    bookLifecycle.deleteBook(existing)
  }

  @DeleteMapping("v1/books/{bookId}/cover")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a book cover by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteBookCover(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") bookId: String
  ) {
    val existing = bookRepository.findByIdOrNull(bookId)
      ?: throw IdDoesNotExistException("Book not found")

    val library = libraryRepository.findById(bookRepository.getLibraryIdOrNull(existing.id)!!)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    bookCoverLifecycle.deleteCover(bookId)
  }
}