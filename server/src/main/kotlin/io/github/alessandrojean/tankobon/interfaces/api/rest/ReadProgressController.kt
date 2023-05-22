package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.ReadProgressRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.ReadProgressLifecycle
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReadProgressCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReadProgressEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReadProgressUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionReadProgress
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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
import io.swagger.v3.oas.annotations.tags.Tag as SwaggerTag

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@SwaggerTag(name = "ReadProgress", description = "Operations regarding read progress")
class ReadProgressController(
  private val libraryRepository: LibraryRepository,
  private val userRepository: TankobonUserRepository,
  private val bookRepository: BookRepository,
  private val readProgressRepository: ReadProgressRepository,
  private val readProgressLifecycle: ReadProgressLifecycle,
  private val referenceExpansion: ReferenceExpansion,
) {

  @PageableAsQueryParam
  @GetMapping("v1/users/{userId}/read-progresses")
  @Operation(summary = "Get all read progresses from a user", security = [SecurityRequirement(name = "Basic Auth")])
  fun getReadProgressesByUser(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    userId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionReadProgress> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<ReadProgressEntityDto> {
    val user = libraryRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    if (user.id != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val sort = if (page.sort.isSorted) page.sort else Sort.unsorted()
    val pageRequest = PageRequest.of(page.pageNumber, page.pageSize, sort)

    val readProgressesPage = readProgressRepository
      .findByUserId(user.id, pageRequest)
      .map { it.toDto() }

    val readProgresses = referenceExpansion.expand(readProgressesPage.content, includes)

    return PageImpl(readProgresses, readProgressesPage.pageable, readProgressesPage.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("v1/books/{bookId}/read-progresses")
  @Operation(
    summary = "Get all read progresses from a book by the user",
    security = [SecurityRequirement(name = "Basic Auth")],
  )
  fun getReadProgressesByBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    bookId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionReadProgress> = emptySet(),
    sort: Sort,
  ): SuccessCollectionResponseDto<ReadProgressEntityDto> {
    if (bookRepository.findByIdOrNull(bookId) == null) {
      throw IdDoesNotExistException("Book not found")
    }

    val libraryId = bookRepository.getLibraryIdOrNull(bookId)!!
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val readProgresses = readProgressRepository
      .findByBookAndUserId(bookId, principal.user.id, sort)
      .map { it.toDto() }

    val expanded = referenceExpansion.expand(
      entities = readProgresses,
      relationsToExpand = includes,
    )

    return SuccessCollectionResponseDto(expanded)
  }

  @GetMapping("v1/read-progresses/{readProgressId}")
  @Operation(summary = "Get a read progress by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneReadProgress(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    readProgressId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<ReferenceExpansionReadProgress> = emptySet(),
  ): SuccessEntityResponseDto<ReadProgressEntityDto> {
    val readProgress = readProgressRepository.findByIdOrNull(readProgressId)
      ?: throw IdDoesNotExistException("Read progress not found")

    if (readProgress.userId != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val expanded = referenceExpansion.expand(
      entity = readProgress.toDto(),
      relationsToExpand = includes,
    )

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping("v1/read-progresses")
  @Operation(summary = "Create a new read progress", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneReadProgress(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    readProgress: ReadProgressCreationDto,
  ): SuccessEntityResponseDto<ReadProgressEntityDto> {
    val userId = readProgress.user ?: principal.user.id
    userRepository.findByIdOrNull(userId)
      ?: throw RelationIdDoesNotExistException("User not found")

    if (userId != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val book = bookRepository.findByIdOrNull(readProgress.book)
      ?: throw RelationIdDoesNotExistException("Book not found")

    val libraryId = bookRepository.getLibraryIdOrNull(book.id)!!
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val created = readProgressLifecycle.addReadProgress(
      ReadProgress(
        page = readProgress.page,
        startedAt = readProgress.startedAt,
        finishedAt = readProgress.finishedAt,
        isCompleted = readProgress.isCompleted,
        bookId = readProgress.book,
        userId = userId,
      ),
    )

    return SuccessEntityResponseDto(created.toDto())
  }

  @DeleteMapping("v1/read-progresses/{readProgressId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a read progress by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteOneReadProgress(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    readProgressId: String,
  ) {
    val existing = readProgressRepository.findByIdOrNull(readProgressId)
      ?: throw IdDoesNotExistException("Read progress not found")

    if (existing.userId != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    readProgressLifecycle.deleteReadProgress(existing)
  }

  @PutMapping("v1/read-progress/{readProgressId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Modify a read progress by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateOneReadProgress(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable
    @UUID(version = [4])
    @Schema(format = "uuid")
    readProgressId: String,
    @Valid @RequestBody
    readProgress: ReadProgressUpdateDto,
  ) {
    val existing = readProgressRepository.findByIdOrNull(readProgressId)
      ?: throw IdDoesNotExistException("Read progress not found")

    if (existing.userId != principal.user.id && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val toUpdate = existing.copy(
      page = readProgress.page,
      startedAt = readProgress.startedAt,
      finishedAt = readProgress.finishedAt,
      isCompleted = readProgress.isCompleted,
    )

    readProgressLifecycle.updateReadProgress(toUpdate)
  }
}
