package io.github.alessandrojean.tankobon.infrastructure.importer.openlibrary

import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookContributor
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import org.springframework.boot.info.BuildProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component("openLibraryImporterProvider")
class OpenLibraryImporterProvider(
  private val webClient: WebClient,
  private val buildProperties: BuildProperties,
) : ImporterProvider() {

  override val baseUrl: String = "https://openlibrary.org"

  override val key: ImporterSource = ImporterSource.OPEN_LIBRARY

  override fun searchByIsbn(isbn: String): Collection<ImporterBookResult> {
    val bibKey = "ISBN:$isbn"

    val results = webClient.get()
      .uri("$baseUrl/api/books") {
        it.queryParam("bibkeys", bibKey)
          .queryParam("jscmd", "data")
          .queryParam("format", "json")
          .build()
      }
      .headers {
        it[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
        it[HttpHeaders.USER_AGENT] = "Tankobon/${buildProperties.version}"
      }
      .retrieve()
      .bodyToMono(object : ParameterizedTypeReference<OpenLibraryResultDto>() {})
      .block(API_TIMEOUT)

    if (results.orEmpty().isEmpty() || !results!!.containsKey(bibKey)) {
      return emptyList()
    }

    val details = runCatching {
      webClient.get()
        .uri("$baseUrl/isbn/$isbn.json")
        .headers {
          it[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
          it[HttpHeaders.USER_AGENT] = "Tankobon/${buildProperties.version}"
        }
        .retrieve()
        .bodyToMono(OpenLibraryBookDetailsDto::class.java)
        .block(API_TIMEOUT)
    }

    return listOf(results[bibKey]!!.toDomain(details.getOrNull()))
  }

  private fun OpenLibraryBookDto.toDomain(details: OpenLibraryBookDetailsDto?): ImporterBookResult {
    val dimensions = details?.physicalDimensions
      ?.removeSuffix(" centimeters")
      ?.split(" x ")
      ?.mapNotNull(String::toFloatOrNull)
      .orEmpty()

    return ImporterBookResult(
      id = key.removePrefix("/books/"),
      isbn = identifiers["isbn_13"]?.get(0) ?: identifiers["isbn_10"]!![0],
      title = title.trim(),
      contributors = authors.orEmpty().map { ImporterBookContributor(it.name, "Author") } +
              details?.contributors.orEmpty().map { ImporterBookContributor(it.name, it.role.orEmpty().ifEmpty { "Author" }) },
      publisher = publishers[0].name,
      dimensions = if (
        details?.physicalDimensions.orEmpty().endsWith(" centimeters") &&
        dimensions.size == 3
      ) {
        Dimensions(dimensions[1], dimensions[0])
      } else {
        null
      },
      synopsis = details?.description?.value.orEmpty(),
      pageCount = pageCount ?: 0,
      coverUrl = cover?.large.orEmpty(),
      provider = ImporterSource.OPEN_LIBRARY,
    )
  }

  companion object {
    private val API_TIMEOUT = 3.seconds.toJavaDuration()
  }

}