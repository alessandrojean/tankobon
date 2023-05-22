package io.github.alessandrojean.tankobon.infrastructure.validation

import com.ibm.icu.util.ULocale
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [Bcp47Validator::class])
@Target(
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.FIELD,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.ANNOTATION_CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Bcp47(
  val message: String = "must be a valid BCP 47 language tag",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class Bcp47Validator : ConstraintValidator<Bcp47, String> {

  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    return Bcp47TagValidator.isValid(value)
  }
}

object Bcp47TagValidator {
  val languages by lazy { ULocale.getISOLanguages().toSet() }
  val regions by lazy { ULocale.getISOCountries().toSet() }

  fun isValid(value: String): Boolean {
    return ULocale.forLanguageTag(value).let {
      it.language.isNotBlank() && languages.contains(it.language) &&
        it.country.isNotBlank() && regions.contains(it.country)
    }
  }
}
