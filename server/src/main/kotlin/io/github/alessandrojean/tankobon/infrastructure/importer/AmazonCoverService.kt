package io.github.alessandrojean.tankobon.infrastructure.importer

import io.github.alessandrojean.tankobon.infrastructure.cache.AmazonCoverCache
import org.hibernate.validator.constraints.ISBN
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import javax.imageio.ImageIO

@Service
@Validated
class AmazonCoverService(
  private val webClient: WebClient,
  private val amazonCoverCache: AmazonCoverCache,
) {

  companion object {
    private const val AMAZON_IMAGES_URL = "https://images-na.ssl-images-amazon.com/images/P"
    private const val AMAZON_IMAGE_FULL_SIZE = "SCRM"
    private const val AMAZON_IMAGE_SIZE = "SL700"
  }

  fun getCoverUrl(@ISBN isbn: String, size: String = AMAZON_IMAGE_SIZE): String {
    val arguments = listOf(AMAZON_IMAGE_FULL_SIZE, AMAZON_IMAGE_SIZE)
      .filter(String::isNotEmpty)

    val argumentsUrl = if (arguments.isNotEmpty()) {
      arguments.joinToString("_", prefix = "._", postfix = "_")
    } else {
      ""
    }

    return "$AMAZON_IMAGES_URL/${isbn.toIsbn10()}.01$argumentsUrl.jpg"
  }

  suspend fun findCoverUrl(@ISBN isbn: String): String? {
    val fromCache = amazonCoverCache[isbn]

    if (fromCache != null) {
      return fromCache.ifEmpty { null }
    }

    val coverUrl = getCoverUrl(isbn, "SL100")

    val bodyStream = webClient.get()
      .uri(coverUrl)
      .retrieve()
      .awaitBody<ByteArray>()
      .inputStream()

    val image = ImageIO.read(bodyStream)

    val checkedUrl = if (image.width > 1 && image.height > 1) {
      coverUrl.replace("SL100", AMAZON_IMAGE_SIZE)
    } else {
      null
    }

    amazonCoverCache[isbn] = checkedUrl

    return checkedUrl
  }
}