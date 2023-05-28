package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SearchDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessObjectResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
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
@Tag(name = "Search", description = "Operations regarding search")
class SearchController(
  private val luceneHelper: LuceneHelper,
  private val libraryRepository: LibraryRepository,
  private val publisherRepository: PublisherRepository,
  private val seriesRepository: SeriesRepository,
  private val storeRepository: StoreRepository,
  private val personRepository: PersonRepository,
  private val tagRepository: TagRepository,
  private val collectionRepository: CollectionRepository,
  private val bookDtoRepository: BookDtoRepository,
) {

  @GetMapping("v1/libraries/{libraryId}/search")
  @Operation(
    summary = "Search through all entities",
    description = """
Search will be done on all entities supported. The results will be filtered to include
only the entities that belongs to the library specified by the `libraryId` path parameter.
The search is backed by Apache Lucene, so `search` accepts a Lucene query syntax if provided.
    """,
    security = [SecurityRequirement(name = "Basic Auth")],
  )
  fun search(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(name = "search", required = true)
    @NotBlank
    searchTerm: String? = null,
    @PathVariable
    @Schema(format = "uuid")
    @UUID(version = [4])
    libraryId: String,
    @RequestParam(required = false, defaultValue = "10") size: Int = 10,
  ): SuccessObjectResponseDto<SearchDto> {
    val library = libraryRepository.findByIdOrNull(libraryId)
      ?: throw IdDoesNotExistException("Library not found")

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val results = luceneHelper.searchEntitiesIds(searchTerm)!!

    return SuccessObjectResponseDto(
      SearchDto(
        books = bookDtoRepository
          .findAllByIds(results[LuceneEntity.Book].orEmpty().take(size), libraryId)
          .toList(),
        publishers = publisherRepository
          .findAllByIds(results[LuceneEntity.Publisher].orEmpty().take(size), libraryId)
          .map { it.toDto() },
        series = seriesRepository
          .findAllByIds(results[LuceneEntity.Series].orEmpty().take(size), libraryId)
          .map { it.toDto() },
        stores = storeRepository
          .findAllByIds(results[LuceneEntity.Store].orEmpty().take(size), libraryId)
          .map { it.toDto() },
        people = personRepository
          .findAllByIds(results[LuceneEntity.Person].orEmpty().take(size), libraryId)
          .map { it.toDto() },
        tags = tagRepository
          .findAllByIds(results[LuceneEntity.Tag].orEmpty().take(size), libraryId)
          .map { it.toDto() },
        collections = collectionRepository
          .findAllByIds(results[LuceneEntity.Collection].orEmpty().take(size), libraryId)
          .map { it.toDto() },
      ),
    )
  }
}
