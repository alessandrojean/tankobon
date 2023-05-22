package io.github.alessandrojean.tankobon.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.Year
import kotlin.reflect.KClass

@Constraint(validatedBy = [MaxCurrentYearValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaxCurrentYear(
  val message: String = "year must be lesser or equal than the current year",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class MaxCurrentYearValidator : ConstraintValidator<MaxCurrentYear, Int?> {
  override fun isValid(value: Int?, context: ConstraintValidatorContext): Boolean {
    if (value == null) {
      return true
    }

    return value <= Year.now().value
  }
}
