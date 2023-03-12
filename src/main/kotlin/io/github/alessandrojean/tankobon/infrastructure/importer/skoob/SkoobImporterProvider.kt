package io.github.alessandrojean.tankobon.infrastructure.importer.skoob

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookContributor
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterBookResult
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterProvider
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component("skoobImporterProvider")
class SkoobImporterProvider(
  private val webClient: WebClient,
) : ImporterProvider() {

  override val key: ImporterSource = ImporterSource.SKOOB

  override val name: String = "Skoob"

  override val url: String = "https://skoob.com.br"

  override val baseUrl: String = "https://api.skoob.com.br/api2"

  override val description: Map<String, String> = mapOf(
    "en-US" to """
      Skoob is a collaborative social network for Brazilian readers, launched in 2009.
      It's currently owned by Lojas Americanas, a private retail chain, since 2021.
    """.trimIndent(),
    "pt-BR" to """
      O Skoob é uma rede social colaborativa para leitores, lançada em 2009. Atualmente
      pertence as Lojas Americanas, uma rede privada de varejo, desde 2021.
    """.trimIndent()
  )

  override val language: String = "pt-BR"

  override suspend fun searchByIsbn(isbn: String): Collection<ImporterBookResult> {
    return webClient.get()
      .uri(baseUrl) {
        it
          .pathSegment(
            "book",
            "search",
            "term:$isbn",
            "limit:8",
            "page:1",
            "isbn:true",
            "ranking:true"
          )
          .build()
      }
      .headers {
        it[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
        it[HttpHeaders.USER_AGENT] = API_USER_AGENT
      }
      .retrieve()
      .awaitBodyOrNull<SkoobResponseDto<List<SkoobBookDto>>>()
      ?.toDomain()
      .orEmpty()
  }

  private fun SkoobResponseDto<List<SkoobBookDto>>.toDomain(): List<ImporterBookResult> {
    if (!success) {
      return emptyList()
    }

    return response.orEmpty()
      .distinctBy(SkoobBookDto::bookId)
      .map { it.toDomain() }
  }

  private fun SkoobBookDto.toDomain(): ImporterBookResult = ImporterBookResult(
    id = bookId.toString(),
    isbn = isbn.toString(),
    title = title.orEmpty(),
    contributors = author.orEmpty()
      .split(",", "&")
      .map { ImporterBookContributor(it.trim(), "Author") },
    publisher = publisher.orEmpty(),
    synopsis = synopsis.orEmpty().trim(),
    pageCount = pageCount ?: 0,
    coverUrl = coverUrl?.substringAfter("format(jpeg)/"),
    url = this@SkoobImporterProvider.url + url,
    provider = ImporterSource.SKOOB,
  )

  companion object {
    private const val API_USER_AGENT = "okhttp/3.12.12"
  }

}