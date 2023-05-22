package io.github.alessandrojean.tankobon.infrastructure.image

import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {}

@Service
class ImageConverter {

  val supportedReadFormats by lazy { ImageIO.getReaderFormatNames().toList() }
  val supportedReadMediaTypes by lazy { ImageIO.getReaderMIMETypes().toList() }
  val supportedWriteFormats by lazy { ImageIO.getWriterFormatNames().toList() }
  val supportedWriteMediaTypes by lazy { ImageIO.getWriterMIMETypes().toList() }

  init {
    logger.info { "Supported read formats: $supportedReadFormats" }
    logger.info { "Supported read mediaTypes: $supportedReadMediaTypes" }
    logger.info { "Supported write formats: $supportedWriteFormats" }
    logger.info { "Supported write mediaTypes: $supportedWriteMediaTypes" }
  }

  private val supportsTransparency = arrayOf("png")

  fun convertImage(imageBytes: ByteArray, format: String): ByteArray = ByteArrayOutputStream().use { baos ->
    Thumbnails.of(imageBytes.inputStream())
      .scale(1.0)
      .imageType(BufferedImage.TYPE_INT_RGB)
      .outputFormat(format)
      .outputQuality(0.95f)
      .toOutputStream(baos)

    baos.toByteArray()
  }

  fun resizeImage(imageBytes: ByteArray, format: String, size: Int, quality: Float = 0.95f): ByteArray = ByteArrayOutputStream().use { baos ->
    Thumbnails.of(imageBytes.inputStream())
      .width(size)
      .imageType(BufferedImage.TYPE_INT_ARGB)
      .outputFormat(format)
      .outputQuality(quality)
      .toOutputStream(baos)

    baos.toByteArray()
  }

  private fun containsAlphaChannel(image: BufferedImage): Boolean = image.colorModel.hasAlpha()

  private fun containsTransparency(image: BufferedImage): Boolean {
    for (x in 0 until image.width) {
      for (y in 0 until image.height) {
        val pixel = image.getRGB(x, y)

        if (pixel shr 24 == 0x00) {
          return true
        }
      }
    }

    return false
  }
}
