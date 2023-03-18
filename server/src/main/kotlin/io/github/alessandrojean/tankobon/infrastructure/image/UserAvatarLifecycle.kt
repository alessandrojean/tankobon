package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ImageDetails
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
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
class UserAvatarLifecycle(
  properties: TankobonProperties,
  private val userRepository: TankobonUserRepository,
  private val imageConverter: ImageConverter,
  private val eventPublisher: EventPublisher,
) {

  companion object {
    private const val AVATARS_DIR = "avatars"
    private val THUMBNAIL_REGEX = ".*(\\.\\d+)\\.jpg\$".toRegex()
  }

  val avatarsDirectoryPath: Path = Paths.get(properties.imagesDir, AVATARS_DIR)
  val thumbnailSizes = arrayOf(64, 128, 256)
  val maxSize = 1024

  @Throws(IdDoesNotExistException::class, IOException::class)
  fun createAvatar(userId: String, file: ByteArray) {
    if (userRepository.findByIdOrNull(userId) == null) {
      throw IdDoesNotExistException("User not found")
    }

    if (!Files.exists(avatarsDirectoryPath)) {
      logger.info { "Creating the user avatars directory in ${avatarsDirectoryPath.toAbsolutePath()}" }
      Files.createDirectories(avatarsDirectoryPath)
    }

    logger.info { "Copying the avatar file for the user $userId" }

    val updating = hasAvatar(userId)

    val resizedFile = file.let {
      val (width, height) = it.getWidthAndHeight()

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

    Files.copy(resizedFile.inputStream(), userId.toAvatarFilePath(), StandardCopyOption.REPLACE_EXISTING)

    createThumbnails(userId)

    getAvatarDetails(userId)?.let {
      eventPublisher.publishEvent(if (updating) DomainEvent.UserAvatarUpdated(it) else DomainEvent.UserAvatarAdded(it))
    }
  }

  @Throws(IOException::class)
  fun deleteAvatar(userId: String) {
    logger.info { "Deleting the user avatar and thumbnails for $userId" }

    val details = getAvatarDetails(userId)
    Files.deleteIfExists(userId.toAvatarFilePath())

    thumbnailSizes.forEach { size -> Files.deleteIfExists(userId.toThumbnailFilePath(size)) }

    details?.let { eventPublisher.publishEvent(DomainEvent.UserAvatarDeleted(it)) }
  }

  @Throws(IOException::class)
  fun hasAvatar(userId: String): Boolean = Files.exists(userId.toAvatarFilePath())

  @Throws(IOException::class)
  fun getAvatarDetails(userId: String): ImageDetails? {
    val userAvatarFilePath = userId.toAvatarFilePath()

    if (Files.notExists(userAvatarFilePath)) {
      return null
    }

    val image = ImageIO.read(userAvatarFilePath.toFile())
    val attributes = Files.readAttributes(userAvatarFilePath, BasicFileAttributes::class.java)

    return ImageDetails(
      id = userId,
      fileName = userAvatarFilePath.fileNameString(),
      versions = mapOf(
        "original" to userAvatarFilePath.fileNameString(),
        *thumbnailSizes.map { it.toString() to userId.toThumbnailFilePath(it).fileNameString() }.toTypedArray(),
      ),
      width = image.width,
      height = image.height,
      aspectRatio = image.aspectRatio(),
      format = userAvatarFilePath.extension,
      mimeType = URLConnection.guessContentTypeFromName(userAvatarFilePath.fileNameString()),
      timeHex = attributes.lastModifiedTime().toMillis().toString(16),
    )
  }

  fun count(): Long {
    if (avatarsDirectoryPath.notExists()) {
      return 0L
    }

    return Files.list(avatarsDirectoryPath)
      .filter { it.toFile().isFile && !it.fileNameString().matches(THUMBNAIL_REGEX) }
      .count()
  }

  @JmsListener(destination = TOPIC_EVENTS, containerFactory = TOPIC_FACTORY)
  fun consumeEvents(event: DomainEvent) {
    if (event !is DomainEvent.UserDeleted) {
      return
    }

    try {
      deleteAvatar(event.user.id)
    } catch (e: IOException) {
      logger.error { "Failed to delete the avatar and thumbnails for ${event.user.id}" }
    }
  }

  private fun createThumbnails(bookId: String) {
    val coverFilePath = avatarsDirectoryPath.resolve("$bookId.jpg")
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

  private fun String.toAvatarFilePath() = avatarsDirectoryPath.resolve("$this.jpg")

  private fun String.toThumbnailFilePath(size: Int) = avatarsDirectoryPath.resolve("$this.$size.jpg")

  private fun Path.fileNameString() = fileName.toString()

  @Throws(IOException::class)
  private fun ByteArray.getWidthAndHeight(): Pair<Int, Int> = ImageIO.read(inputStream()).let { it.width to it.height }

}