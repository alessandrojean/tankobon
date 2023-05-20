package io.github.alessandrojean.tankobon.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.net.URL
import kotlin.reflect.KClass

@Constraint(validatedBy = [UrlMultipleHostsValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class UrlMultipleHosts(
  val message: String = "URL should be valid and be part of the allowed hosts list",
  val allowedHosts: Array<String> = [],
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class UrlMultipleHostsValidator : ConstraintValidator<UrlMultipleHosts, String?> {
  private lateinit var allowedHosts: List<String>

  override fun initialize(constraintAnnotation: UrlMultipleHosts) {
    allowedHosts = constraintAnnotation.allowedHosts.map { it.removePrefix("www.").lowercase() }
  }

  override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
    if (value.isNullOrEmpty()) {
      return true
    }

    val urlResult = runCatching { URL(value) }
    val url = urlResult.getOrNull() ?: return false
    val host = url.host.removePrefix("www.").lowercase()

    if (allowedHosts.isNotEmpty() && !allowedHosts.contains(host)) {
      return false
    }

    return true
  }
}
