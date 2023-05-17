package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Image
import io.github.alessandrojean.tankobon.domain.persistence.ImageRepository
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import io.trbl.blurhash.BlurHash
import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.jms.annotation.JmsListener
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.ZoneId
import javax.imageio.ImageIO
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

private val logger = KotlinLogging.logger {}

abstract class EntityImageLifecycle(
  protected val entityName: String,
  protected val eventPublisher: EventPublisher,
  protected val imageConverter: ImageConverter,
  protected val imageRepository: ImageRepository,
) {

  /**
   * The directory where this entity images will be created.
   */
  protected abstract val imagesDirectoryPath: Path

  /**
   * The sizes to generate the thumbnails.
   */
  protected abstract val thumbnailSizes: Array<Int>

  /**
   * The maximum width accepted during upload.
   */
  protected open val maxSize = 1024

  protected enum class PublishEventType { CREATED, UPDATED, DELETED }

  protected abstract fun entityExistsById(entityId: String): Boolean

  protected abstract fun createEvent(imageDetails: Image, type: PublishEventType): DomainEvent

  protected abstract fun isDeleteEvent(event: DomainEvent): Boolean

  protected abstract fun getEntityIdFromDeleteEvent(event: DomainEvent): String

  @Throws(IdDoesNotExistException::class, IOException::class)
  fun createImage(entityId: String, file: ByteArray) {
    if (!entityExistsById(entityId)) {
      throw IdDoesNotExistException("Entity with id $entityId not found")
    }

    if (!Files.exists(imagesDirectoryPath)) {
      logger.info { "Creating an images directory in ${imagesDirectoryPath.toAbsolutePath()}" }
      Files.createDirectories(imagesDirectoryPath)
    }

    logger.info { "Copying the image file for the $entityName $entityId" }

    val updating = hasImage(entityId)

    val resizedFile = file.let {
      val (width, height) = file.getWidthAndHeight()

      if (width < maxSize && height < maxSize) {
        return@let imageConverter.convertImage(it, "jpeg")
      }

      ByteArrayOutputStream().use { baos ->
        Thumbnails.of(it.inputStream())
          .apply { if (width > height) width(maxSize) else height(maxSize) }
          .imageType(BufferedImage.TYPE_INT_ARGB)
          .outputFormat("jpeg")
          .outputQuality(1f)
          .toOutputStream(baos)

        baos.toByteArray()
      }
    }

    val destination = entityId.toImageFilePath()

    Files.copy(resizedFile.inputStream(), destination, StandardCopyOption.REPLACE_EXISTING)

    val image = ImageIO.read(destination.inputStream())
    val folderName = imagesDirectoryPath.fileNameString()
    val imageDomain = Image(
      id = entityId,
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
      val existingImage = getImageDetails(entityId)!!

      imageRepository.update(
        imageDomain.copy(createdAt = existingImage.createdAt)
      )
    } else {
      imageRepository.insert(imageDomain)
    }

    createThumbnails(entityId)
    getImageDetails(entityId)?.let {
      val event = createEvent(it, if (updating) PublishEventType.UPDATED else PublishEventType.CREATED)
      eventPublisher.publishEvent(event)
    }
  }

  @Throws(IOException::class)
  fun deleteImage(entityId: String) {
    logger.info { "Deleting the $entityName image and thumbnails for $entityId" }

    val details = getImageDetails(entityId)
    Files.deleteIfExists(entityId.toImageFilePath())

    thumbnailSizes.forEach { size -> Files.deleteIfExists(entityId.toThumbnailFilePath(size)) }

    imageRepository.delete(entityId)

    details?.let { eventPublisher.publishEvent(createEvent(it, PublishEventType.DELETED)) }
  }

  @Throws(IOException::class)
  fun hasImage(entityId: String): Boolean = imageRepository.findByIdOrNull(entityId) != null

  @Throws(IOException::class)
  fun getEntitiesWithImages(entityIds: Collection<String>): Map<String, Boolean> {
    val images = imageRepository.findAllByIds(entityIds).map { it.id }.toHashSet()

    return entityIds.associateWith { it in images }
  }

  @Throws(IOException::class)
  fun getImageDetails(entityId: String): Image? {
    val image = imageRepository.findByIdOrNull(entityId)
      ?: return null

    return image.copy(
      versions = mapOf(
        "original" to image.fileName,
        *thumbnailSizes
          .map { size ->
            val sizeString = size.toString()
            sizeString to image.fileName.replace(".", ".$sizeString.")
          }
          .toTypedArray()
      )
    )
  }

  fun count(): Long = imageRepository.countByFolderName(imagesDirectoryPath.fileNameString())

  @JmsListener(destination = TOPIC_EVENTS, containerFactory = TOPIC_FACTORY)
  fun consumeEvents(event: DomainEvent) {
    if (!isDeleteEvent(event)) {
      return
    }

    val entityId = getEntityIdFromDeleteEvent(event)

    try {
      deleteImage(entityId)
    } catch (e: IOException) {
      logger.error { "Failed to delete the $entityName image and thumbnails for $entityId" }
    }
  }

  protected fun createThumbnails(entityId: String) {
    val imageFilePath = imagesDirectoryPath.resolve("$entityId.jpg")
    val imageFileBytes = imageFilePath.readBytes()

    thumbnailSizes.forEach { size ->
      imageConverter.resizeImage(imageFileBytes, "jpeg", size).let {
        Files.copy(it.inputStream(), entityId.toThumbnailFilePath(size), StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  private fun greatestCommonDivisor(a: Int, b: Int): Int =
    if (b == 0) a else greatestCommonDivisor(b, a % b)

  fun BufferedImage.aspectRatio(): String {
    val gcd = greatestCommonDivisor(width, height)

    return "${width / gcd} / ${height / gcd}"
  }

  protected fun String.toImageFilePath(): Path = imagesDirectoryPath.resolve("$this.jpg")

  protected fun String.toThumbnailFilePath(size: Int): Path = imagesDirectoryPath.resolve("$this.$size.jpg")

  protected fun Path.fileNameString() = fileName.toString()

  @Throws(IOException::class)
  private fun ByteArray.getWidthAndHeight(): Pair<Int, Int> = ImageIO.read(inputStream()).let { it.width to it.height }

  companion object {
    protected val THUMBNAIL_REGEX = ".*(\\.\\d+)\\.jpg\$".toRegex()
  }

}