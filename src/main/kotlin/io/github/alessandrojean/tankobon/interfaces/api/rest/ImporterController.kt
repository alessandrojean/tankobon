package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterLifecycle
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ImportDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ImporterEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ImporterSourceEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.hibernate.validator.constraints.ISBN
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(
  name = "Importer",
  description = """
Tankobon provides an easy way to import book metadata from external sources such as
[Open Library](https://openlibrary.org/). The search is done by using the book
[ISBN](https://en.wikipedia.org/wiki/ISBN), which can be either in the ISBN-10 
or in the ISBN-13 format.

Books can be imported to an existing user library specified through the
collection ID in the request. The book created will contains the metadata from the
source without any special and additional data processing, and the cover will
be downloaded to the file system as well if available in the source. Missing
information such as the store, series, paid price, and billing dates will need
to be set manually later as the sources can't provide these metadata.

Imported books will contain the source information saved, so the metadata can be
synced eventually if needed in the future.

## Available sources

You can find below a relation of the existing sources. They can also be retrieved
programmatically by the API if needed, with extra metadata such as a multi language description.

| Key            | URL                     | Description                                         |
| -------------- | ----------------------- | --------------------------------------------------- |
| `CBL`          | https://cblservicos.org | Official Brazilian ISBN agency.                     |
| `OPEN_LIBRARY` | https://openlibrary.org | Book cataloging archive by Internet Archive.        |
| `SKOOB`        | https://skoob.com.br    | Collaborative social network for Brazilian readers. | 
  """
)
class ImporterController(
  private val importerProviders: List<ImporterProvider>,
  private val collectionRepository: CollectionRepository,
  private val libraryRepository: LibraryRepository,
  private val importerLifecycle: ImporterLifecycle,
) {

  @GetMapping("v1/importer/sources")
  @Operation(summary = "Get all external sources", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllSources(): SuccessCollectionResponseDto<ImporterSourceEntityDto> {
    val sources = importerProviders.map { it.toDto() }
      .sortedBy { it.attributes.name }

    return SuccessCollectionResponseDto(sources)
  }

  @GetMapping("v1/importer/sources/{sourceId}")
  @Operation(summary = "Get a source by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneSource(@PathVariable sourceId: String): SuccessEntityResponseDto<ImporterSourceEntityDto> {
    val source = importerProviders.firstOrNull { it.key.name == sourceId }
      ?: throw IdDoesNotExistException("Source not found")

    return SuccessEntityResponseDto(source.toDto())
  }

  @GetMapping("v1/importer/search/{isbn}")
  @Operation(
    summary = "Search a book by its ISBN in the external sources",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  suspend fun searchByIsbn(
    @PathVariable
    @ISBN(type = ISBN.Type.ANY)
    @Parameter(description = "Can be a ISBN-13 or ISBN-10 code, with or without dashes")
    @Schema(format = "isbn")
    isbn: String,
    @RequestParam(required = false, defaultValue = "") sources: Set<ImporterSource>,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType>,
  ): SuccessCollectionResponseDto<ImporterEntityDto> {
    val results = coroutineScope {
      importerProviders
        .filter { it.key in sources }
        .ifEmpty { importerProviders }
        .map { source ->
          async(Dispatchers.IO) {
            runCatching { source.searchByIsbn(isbn) }
          }
        }
        .awaitAll()
        .flatMap { it.getOrElse { emptyList() } }
    }

    val expanded = if (includes.contains(RelationshipType.IMPORTER_SOURCE)) {
      results.map { book ->
        book.toDto(importerProviders.first { it.key == book.provider }.toAttributesDto())
      }
    } else {
      results.map { it.toDto() }
    }

    return SuccessCollectionResponseDto(expanded)
  }

  @PostMapping("v1/importer/import")
  @Operation(
    summary = "Import an external book into a collection",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  suspend fun importOneBook(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestBody import: ImportDto,
  ): SuccessEntityResponseDto<BookEntityDto> {
    val libraryId = collectionRepository.getLibraryIdOrNull(import.collection)
      ?: throw RelationIdDoesNotExistException("Collection not found")
    val library = libraryRepository.findById(libraryId)

    if (!principal.user.canAccessLibrary(library)) {
      throw UserDoesNotHaveAccessException()
    }

    val result = coroutineScope {
      runCatching {
        importerProviders.first { it.key == import.source }
          .searchByIsbn(import.isbn)
          .firstOrNull { it.id == import.id }
      }
    }

    if (result.isFailure) {
      throw result.exceptionOrNull()!!
    }

    val bookResult = result.getOrNull()
      ?: throw IdDoesNotExistException("Book not found in the source")

    val book = importerLifecycle.importToCollection(
      collectionId = import.collection,
      import = bookResult,
      user = principal.user
    )

    return SuccessEntityResponseDto(book)
  }

}