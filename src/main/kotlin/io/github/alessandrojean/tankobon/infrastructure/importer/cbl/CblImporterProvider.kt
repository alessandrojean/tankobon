package io.github.alessandrojean.tankobon.infrastructure.importer.cbl

import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookContributor
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component("cblImporterProvider")
class CblImporterProvider(
  private val webClient: WebClient,
) : ImporterProvider() {

  override val baseUrl: String = "https://isbn-search-br.search.windows.net/indexes/isbn-index/docs"

  override val key: ImporterSource = ImporterSource.CBL

  override fun searchByIsbn(isbn: String): Collection<ImporterBookResult> {
    val bodyPayload = CblSearchRequestDto(
      count = true,
      facets = listOf("Imprint,count:50", "Authors,count:50"),
      filter = "",
      orderBy = null,
      queryType = "full",
      search = isbn.replace("-", ""),
      searchFields = "FormattedKey,RowKey",
      searchMode = "any",
      select = FIELDS_TO_SELECT.joinToString(","),
      skip = 0,
      top = 12
    )

    return webClient.post()
      .uri("$baseUrl/search?api-version=$API_VERSION")
      .headers {
        it[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
        it["Api-Key"] = API_KEY
        it[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
        it[HttpHeaders.ORIGIN] = API_ORIGIN
        it[HttpHeaders.REFERER] = "$API_ORIGIN/"
        it[HttpHeaders.USER_AGENT] = API_USER_AGENT
      }
      .body(Mono.just(bodyPayload), CblSearchRequestDto::class.java)
      .retrieve()
      .bodyToMono(CblSearchResultDto::class.java)
      .block(API_TIMEOUT)
      ?.toDomain()
      .orEmpty()
  }

  private fun CblSearchResultDto.toDomain(): List<ImporterBookResult> = value.map { it.toDomain() }

  private fun CblRecordDto.toDomain(): ImporterBookResult = ImporterBookResult(
    id = id.orEmpty(),
    isbn = rowKey,
    title = title.trim(),
    contributors = if (roles.orEmpty().size != authors.size) {
      authors.map { ImporterBookContributor(it, "Author") }
    } else {
      authors.zip(roles!!)
        .map { (author, role) -> ImporterBookContributor(author, role) }
    },
    publisher = publisher,
    dimensions = dimensions?.let { dimension ->
      val match = DIMENSION_REGEX.find(dimension)
        ?: return@let null

      Dimensions(
        widthCm = (match.groupValues[1] + "." + match.groupValues[2].ifEmpty{ "0" }).toFloatOrNull() ?: 0f,
        heightCm = (match.groupValues[3] + "." + match.groupValues[4].ifEmpty { "0" }).toFloatOrNull() ?: 0f
      )
    },
    synopsis = synopsis.orEmpty().trim(),
    pageCount = pageCount?.toIntOrNull() ?: 0,
    provider = ImporterSource.CBL,
  )

  companion object {
    private const val API_KEY = "100216A23C5AEE390338BBD19EA86D29"
    private const val API_VERSION = "2016-09-01"
    private const val API_ORIGIN = "https://www.cblservicos.org.br"
    private const val API_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36"
    private val API_TIMEOUT = 3.seconds.toJavaDuration()

    private val FIELDS_TO_SELECT = listOf(
      "Authors",
      "Colection",
      "Countries",
      "Date",
      "Imprint",
      "Title",
      "RowKey",
      "PartitionKey",
      "RecordId",
      "FormattedKey",
      "Subject",
      "Veiculacao",
      "Profissoes",
      "Dimensao",
      "Sinopse"
    )

    private val DIMENSION_REGEX = "(\\d{2})(\\d)?x(\\d{2})(\\d)?$".toRegex()
  }

}