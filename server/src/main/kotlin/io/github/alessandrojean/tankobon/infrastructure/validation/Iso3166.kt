package io.github.alessandrojean.tankobon.infrastructure.validation

import com.ibm.icu.util.ULocale
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [Iso3166Validator::class])
@Target(
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.FIELD,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.ANNOTATION_CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Iso3166(
  val message: String = "must be a valid ISO-3166-1 alpha-2 region code",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class Iso3166Validator : ConstraintValidator<Iso3166, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    return Iso3166CodeValidator.isValid(value)
  }
}

object Iso3166CodeValidator {
  val regions by lazy { ULocale.getISOCountries().toSet() }

  fun isValid(value: String): Boolean {
    return value.length == 2 && regions.contains(value)
  }
}
