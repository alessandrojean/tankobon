package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
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
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
  private val importerProviders: List<ImporterProvider>,
) {

  @GetMapping("v1/importer/sources")
  @Operation(summary = "Get all external sources")
  fun getAllSources(): SuccessCollectionResponseDto<ImporterSourceEntityDto> {
    val sources = importerProviders.map { it.toDto() }
      .sortedBy { it.attributes.name }

    return SuccessCollectionResponseDto(sources)
  }

  @GetMapping("v1/importer/sources/{sourceId}")
  @Operation(summary = "Get a source by its id")
  fun getOneSource(@PathVariable sourceId: String): SuccessEntityResponseDto<ImporterSourceEntityDto> {
    val source = importerProviders.firstOrNull { it.key.name == sourceId }
      ?: throw IdDoesNotExistException("Source not found")

    return SuccessEntityResponseDto(source.toDto())
  }

  @GetMapping("v1/importer/search/{isbn}")
  @Operation(summary = "Search a book by its ISBN in the external sources")
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

}