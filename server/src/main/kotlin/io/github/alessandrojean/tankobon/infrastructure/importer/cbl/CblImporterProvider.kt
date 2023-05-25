package io.github.alessandrojean.tankobon.infrastructure.importer.cbl

import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.model.LengthUnit
import io.github.alessandrojean.tankobon.infrastructure.importer.AmazonCoverService
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookContributor
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import io.github.alessandrojean.tankobon.infrastructure.importer.removeDashes
import io.github.alessandrojean.tankobon.infrastructure.importer.toIsbn10
import io.github.alessandrojean.tankobon.infrastructure.importer.toIsbn13
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import reactor.core.publisher.Mono

@Component("cblImporterProvider")
class CblImporterProvider(
  private val webClient: WebClient,
  private val amazonCoverService: AmazonCoverService,
) : ImporterProvider() {

  override val key: ImporterSource = ImporterSource.CBL

  override val name: String = "CBL"

  override val url: String = "https://cblservicos.org.br"

  override val baseUrl: String = "https://isbn-search-br.search.windows.net/indexes/isbn-index/docs"

  override val description: Map<String, String> = mapOf(
    "en-US" to """
      Câmara Brasileira do Livro (CBL) is a non-profit organization that aims to promote
      the Brazilian publishing market and promote the reading habit. It's the Brazilian
      unique official ISBN agency since 2020.
    """.trimIndent(),
    "pt-BR" to """
      A Câmara Brasileira do Livro (CBL) é uma organização sem fins lucrativos que tem
      como objetivo promover o mercado editorial brasileiro e o hábito de leitura. É
      a única agência oficial do ISBN desde 2020.
    """.trimIndent(),
  )

  override val language: String = "pt-BR"

  override suspend fun searchByIsbn(isbn: String): Collection<ImporterBookResult> {
    val properIsbn = isbn.removeDashes()
    val isbn13 = if (properIsbn.length == 10) isbn.toIsbn13() else isbn
    val isbn10 = if (properIsbn.length == 13) isbn.toIsbn10() else isbn

    val bodyPayload = CblSearchRequestDto(
      count = true,
      facets = listOf("Imprint,count:50", "Authors,count:50"),
      filter = "",
      orderBy = null,
      queryType = "full",
      search = "$isbn13 OR $isbn10",
      searchFields = "FormattedKey,RowKey",
      searchMode = "any",
      select = FIELDS_TO_SELECT.joinToString(","),
      skip = 0,
      top = 12,
    )

    val results = webClient.post()
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
      .awaitBodyOrNull<CblSearchResultDto>()
      ?.toDomain()
      .orEmpty()

    val covers = results.associate {
      val coverUrl = runCatching { amazonCoverService.findCoverUrl(it.isbn) }

      it.isbn to coverUrl.getOrNull()
    }

    return results.map { it.copy(coverUrl = covers[it.isbn]) }
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
        width = (match.groupValues[1] + "." + match.groupValues[2].ifEmpty { "0" }).toFloatOrNull() ?: 0f,
        height = (match.groupValues[3] + "." + match.groupValues[4].ifEmpty { "0" }).toFloatOrNull() ?: 0f,
        depth = 0f,
        unit = LengthUnit.CENTIMETER,
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
      "Sinopse",
    )

    private val DIMENSION_REGEX = "(\\d{2})(\\d)?x(\\d{2})(\\d)?$".toRegex()
  }
}
