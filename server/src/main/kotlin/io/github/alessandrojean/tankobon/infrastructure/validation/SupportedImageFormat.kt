package io.github.alessandrojean.tankobon.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.apache.tika.Tika
import org.springframework.web.multipart.MultipartFile
import javax.imageio.ImageIO
import kotlin.reflect.KClass

@Constraint(validatedBy = [SupportedImageFormatValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SupportedImageFormat(
  val message: String = "must be a supported image format",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class SupportedImageFormatValidator : ConstraintValidator<SupportedImageFormat, MultipartFile> {
  private val supportedReadMediaTypes by lazy { ImageIO.getReaderMIMETypes().toList() }
  private val tika by lazy { Tika() }

  override fun isValid(value: MultipartFile, context: ConstraintValidatorContext): Boolean {
    return tika.detect(value.bytes) in supportedReadMediaTypes
  }
}
