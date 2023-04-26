package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ImageDetails
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

private val logger = KotlinLogging.logger {}

@Component
class BookCoverLifecycle(
  properties: TankobonProperties,
  imageConverter: ImageConverter,
  eventPublisher: EventPublisher,
  private val bookRepository: BookRepository,
  private val webClient: WebClient,
) : EntityImageLifecycle("book", eventPublisher, imageConverter) {

  companion object {
    private const val COVERS_DIR = "covers"
  }

  override val imagesDirectoryPath: Path = Paths.get(properties.imagesDir, COVERS_DIR)
  override val thumbnailSizes = arrayOf(256, 512)

  override fun entityExistsById(entityId: String): Boolean {
    return bookRepository.findByIdOrNull(entityId) != null
  }

  override fun createEvent(imageDetails: ImageDetails, type: PublishEventType): DomainEvent = when (type) {
    PublishEventType.CREATED -> DomainEvent.BookCoverAdded(imageDetails)
    PublishEventType.UPDATED -> DomainEvent.BookCoverUpdated(imageDetails)
    else -> DomainEvent.BookCoverDeleted(imageDetails)
  }

  override fun isDeleteEvent(event: DomainEvent) = event is DomainEvent.BookDeleted

  override fun getEntityIdFromDeleteEvent(event: DomainEvent) = (event as DomainEvent.BookDeleted).book.id

  @Throws(IdDoesNotExistException::class, IOException::class)
  suspend fun downloadCover(bookId: String, coverUrl: String) = withContext(Dispatchers.IO) {
    if (!entityExistsById(bookId)) {
      throw IdDoesNotExistException("Book not found")
    }

    if (!Files.exists(imagesDirectoryPath)) {
      logger.info { "Creating an images directory in ${imagesDirectoryPath.toAbsolutePath()}" }
      Files.createDirectories(imagesDirectoryPath)
    }

    logger.info { "Downloading the cover for the book $bookId" }

    val updating = hasImage(bookId)

    val coverBytes = webClient.get()
      .uri(coverUrl)
      .retrieve()
      .awaitBody<ByteArrayResource>()
      .byteArray

    imageConverter.convertImage(coverBytes, "jpeg").let {
      Files.copy(it.inputStream(), bookId.toImageFilePath(), StandardCopyOption.REPLACE_EXISTING)
    }

    createThumbnails(bookId)

    getImageDetails(bookId)?.let {
      val event = createEvent(it, if (updating) PublishEventType.UPDATED else PublishEventType.CREATED)
      eventPublisher.publishEvent(event)
    }
  }

}