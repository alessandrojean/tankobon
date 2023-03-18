package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ImageDetails
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.notExists
import kotlin.io.path.readBytes

private val logger = KotlinLogging.logger {}

@Component
class BookCoverLifecycle(
  properties: TankobonProperties,
  private val bookRepository: BookRepository,
  private val imageConverter: ImageConverter,
  private val webClient: WebClient,
  private val eventPublisher: EventPublisher,
) {

  companion object {
    private const val COVERS_DIR = "covers"
    private val THUMBNAIL_REGEX = ".*(\\.\\d+)\\.jpg\$".toRegex()
  }

  val coversDirectoryPath: Path = Paths.get(properties.imagesDir, COVERS_DIR)
  val thumbnailSizes = arrayOf(256, 512)

  @Throws(IdDoesNotExistException::class, IOException::class)
  fun createCover(bookId: String, file: ByteArray) {
    if (bookRepository.findByIdOrNull(bookId) == null) {
      throw IdDoesNotExistException("Book not found")
    }

    if (!Files.exists(coversDirectoryPath)) {
      logger.info { "Creating the book covers directory in ${coversDirectoryPath.toAbsolutePath()}" }
      Files.createDirectories(coversDirectoryPath)
    }

    logger.info { "Copying the cover file for the book $bookId" }

    val updating = hasCover(bookId)

    imageConverter.convertImage(file, "jpeg").let {
      Files.copy(it.inputStream(), bookId.toCoverFilePath(), StandardCopyOption.REPLACE_EXISTING)
    }

    createThumbnails(bookId)

    getCoverDetails(bookId)?.let {
      eventPublisher.publishEvent(if (updating) DomainEvent.BookCoverUpdated(it) else DomainEvent.BookCoverAdded(it))
    }
  }

  @Throws(IdDoesNotExistException::class, IOException::class)
  suspend fun createCover(bookId: String, coverUrl: String) = withContext(Dispatchers.IO) {
    if (bookRepository.findByIdOrNull(bookId) == null) {
      throw IdDoesNotExistException("Book not found")
    }

    if (!Files.exists(coversDirectoryPath)) {
      logger.info { "Creating the book covers directory in ${coversDirectoryPath.toAbsolutePath()}" }
      Files.createDirectories(coversDirectoryPath)
    }

    logger.info { "Downloading the cover for the book $bookId" }

    val updating = hasCover(bookId)

    val coverBytes = webClient.get()
      .uri(coverUrl)
      .retrieve()
      .awaitBody<ByteArrayResource>()
      .byteArray

    imageConverter.convertImage(coverBytes, "jpeg").let {
      Files.copy(it.inputStream(), bookId.toCoverFilePath(), StandardCopyOption.REPLACE_EXISTING)
    }

    createThumbnails(bookId)

    getCoverDetails(bookId)?.let {
      eventPublisher.publishEvent(if (updating) DomainEvent.BookCoverUpdated(it) else DomainEvent.BookCoverAdded(it))
    }
  }

  @Throws(IOException::class)
  fun deleteCover(bookId: String) {
    logger.info { "Deleting the book covers and thumbnails for $bookId" }

    val details = getCoverDetails(bookId)
    Files.deleteIfExists(bookId.toCoverFilePath())

    thumbnailSizes.forEach { size -> Files.deleteIfExists(bookId.toThumbnailFilePath(size)) }

    details?.let { eventPublisher.publishEvent(DomainEvent.BookCoverDeleted(it)) }
  }

  @Throws(IOException::class)
  fun hasCover(bookId: String): Boolean = Files.exists(bookId.toCoverFilePath())

  @Throws(IOException::class)
  fun getCoverDetails(bookId: String): ImageDetails? {
    val bookCoverFilePath = bookId.toCoverFilePath()

    if (Files.notExists(bookCoverFilePath)) {
      return null
    }

    val image = ImageIO.read(bookCoverFilePath.toFile())
    val attributes = Files.readAttributes(bookCoverFilePath, BasicFileAttributes::class.java)

    return ImageDetails(
      id = bookId,
      fileName = bookCoverFilePath.fileNameString(),
      versions = mapOf(
        "original" to bookCoverFilePath.fileNameString(),
        *thumbnailSizes.map { it.toString() to bookId.toThumbnailFilePath(it).fileNameString() }.toTypedArray(),
      ),
      width = image.width,
      height = image.height,
      aspectRatio = image.aspectRatio(),
      format = bookCoverFilePath.extension,
      mimeType = URLConnection.guessContentTypeFromName(bookCoverFilePath.fileNameString()),
      timeHex = attributes.lastModifiedTime().toMillis().toString(16),
    )
  }

  fun count(): Long {
    if (coversDirectoryPath.notExists()) {
      return 0L
    }

    return Files.list(coversDirectoryPath)
      .filter { it.toFile().isFile && !it.fileNameString().matches(THUMBNAIL_REGEX) }
      .count()
  }

  @JmsListener(destination = TOPIC_EVENTS, containerFactory = TOPIC_FACTORY)
  fun consumeEvents(event: DomainEvent) {
    if (event !is DomainEvent.BookDeleted) {
      return
    }

    try {
      deleteCover(event.book.id)
    } catch (e: IOException) {
      logger.error { "Failed to delete the cover and thumbnails for ${event.book.id}" }
    }
  }

  private fun createThumbnails(bookId: String) {
    val coverFilePath = coversDirectoryPath.resolve("$bookId.jpg")
    val coverFileBytes = coverFilePath.readBytes()

    thumbnailSizes.forEach { size ->
      imageConverter.resizeImage(coverFileBytes, "jpeg", size).let {
        Files.copy(it.inputStream(), bookId.toThumbnailFilePath(size), StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  private fun greatestCommonDivisor(a: Int, b: Int): Int =
    if (b == 0) a else greatestCommonDivisor(b, a % b)

  private fun BufferedImage.aspectRatio(): String {
    val gcd = greatestCommonDivisor(width, height)

    return "${width / gcd} / ${height / gcd}"
  }

  private fun String.toCoverFilePath() = coversDirectoryPath.resolve("$this.jpg")

  private fun String.toThumbnailFilePath(size: Int) = coversDirectoryPath.resolve("$this.$size.jpg")

  private fun Path.fileNameString() = fileName.toString()

}