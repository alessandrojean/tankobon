package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.readBytes

private val logger = KotlinLogging.logger {}

@Component
class BookCoverLifecycle(
  properties: TankobonProperties,
  private val bookRepository: BookRepository,
  private val imageConverter: ImageConverter,
) {

  companion object {
    private const val COVERS_DIR = "covers"
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

    imageConverter.convertImage(file, "jpeg").let {
      Files.copy(it.inputStream(), bookId.toCoverFilePath(), StandardCopyOption.REPLACE_EXISTING)
    }

    createThumbnails(bookId)
  }

  @Throws(IOException::class)
  fun deleteCover(bookId: String) {
    logger.info { "Deleting the book covers and thumbnails for $bookId" }

    Files.deleteIfExists(bookId.toCoverFilePath())

    thumbnailSizes.forEach { size -> Files.deleteIfExists(bookId.toThumbnailFilePath(size)) }
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

  private fun String.toCoverFilePath() = coversDirectoryPath.resolve("$this.jpg")

  private fun String.toThumbnailFilePath(size: Int) = coversDirectoryPath.resolve("$this.$size.jpg")

}