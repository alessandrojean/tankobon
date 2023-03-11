package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import io.github.alessandrojean.tankobon.infrastructure.importer.cbl.CblImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.openlibrary.OpenLibraryImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.skoob.SkoobImporterProvider
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ImporterEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.hibernate.validator.constraints.ISBN
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Importer", description = "Operations regarding importing books from other external sources")
class ImporterController(
  private val cblImporterProvider: CblImporterProvider,
  private val skoobImporterProvider: SkoobImporterProvider,
  private val openLibraryImporterProvider: OpenLibraryImporterProvider,
) {

  private val sourcesMapping = mapOf<ImporterSource, (isbn: String) -> Collection<ImporterBookResult>>(
    ImporterSource.CBL to { cblImporterProvider.searchByIsbn(it) },
    ImporterSource.SKOOB to { skoobImporterProvider.searchByIsbn(it) },
    ImporterSource.OPEN_LIBRARY to { openLibraryImporterProvider.searchByIsbn(it) }
  )

  @GetMapping("v1/importer/search/{isbn}")
  @Operation(summary = "Search a book by its ISBN in the external sources")
  fun searchByIsbn(
    @PathVariable
    @ISBN(type = ISBN.Type.ANY)
    @Parameter(description = "Can be a ISBN-13 or ISBN-10 code")
    @Schema(format = "isbn")
    isbn: String,
    @RequestParam(required = false, defaultValue = "") sources: Set<ImporterSource>,
  ): SuccessCollectionResponseDto<ImporterEntityDto> {
    val results = sources
      .mapNotNull { sourcesMapping[it] }
      .ifEmpty { sourcesMapping.values }
      .flatMap { it.invoke(isbn) }
      .map { it.toDto() }

    return SuccessCollectionResponseDto(results)
  }

}