package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Image
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.ImageRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import io.trbl.blurhash.BlurHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.ZoneId
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.inputStream

private val logger = KotlinLogging.logger {}

@Component
class BookCoverLifecycle(
  properties: TankobonProperties,
  imageConverter: ImageConverter,
  eventPublisher: EventPublisher,
  imageRepository: ImageRepository,
  private val bookRepository: BookRepository,
  private val webClient: WebClient,
) : EntityImageLifecycle("book", eventPublisher, imageConverter, imageRepository) {

  companion object {
    private const val COVERS_DIR = "covers"
  }

  override val imagesDirectoryPath: Path = Paths.get(properties.imagesDir, COVERS_DIR)
  override val thumbnailSizes = arrayOf(256, 512)

  override fun entityExistsById(entityId: String): Boolean {
    return bookRepository.findByIdOrNull(entityId) != null
  }

  override fun createEvent(imageDetails: Image, type: PublishEventType): DomainEvent = when (type) {
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

    val destination = bookId.toImageFilePath()

    imageConverter.convertImage(coverBytes, "jpeg").let {
      Files.copy(it.inputStream(), destination, StandardCopyOption.REPLACE_EXISTING)
    }

    val image = ImageIO.read(destination.inputStream())
    val folderName = imagesDirectoryPath.fileNameString()
    val imageDomain = Image(
      id = bookId,
      fileName = "$folderName/${destination.fileNameString()}",
      width = image.width,
      height = image.height,
      aspectRatio = image.aspectRatio(),
      format = destination.extension,
      mimeType = URLConnection.guessContentTypeFromName(destination.fileNameString()),
      timeHex = LocalDateTime.now()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
        .toString(16),
      blurHash = BlurHash.encode(image),
    )

    if (updating) {
      val existingImage = getImageDetails(bookId)!!

      imageRepository.update(
        imageDomain.copy(createdAt = existingImage.createdAt)
      )
    } else {
      imageRepository.insert(imageDomain)
    }

    createThumbnails(bookId)

    getImageDetails(bookId)?.let {
      val event = createEvent(it, if (updating) PublishEventType.UPDATED else PublishEventType.CREATED)
      eventPublisher.publishEvent(event)
    }
  }

}