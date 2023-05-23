package io.github.alessandrojean.tankobon.infrastructure.validation

import io.github.alessandrojean.tankobon.domain.model.DurationalPerson
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [PersonDateRangeValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PersonDateRangeValidation(
  val message: String = "must contain a valid date range",
  val bornGreaterMessage: String = "must be less than the died date",
  val diedLessMessage: String = "must be greater than the born date",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class PersonDateRangeValidator : ConstraintValidator<PersonDateRangeValidation, DurationalPerson> {
  private lateinit var bornGreaterMessage: String
  private lateinit var diedLessMessage: String

  override fun initialize(constraintAnnotation: PersonDateRangeValidation) {
    bornGreaterMessage = constraintAnnotation.bornGreaterMessage
    diedLessMessage = constraintAnnotation.diedLessMessage
  }

  override fun isValid(value: DurationalPerson, context: ConstraintValidatorContext): Boolean {
    if (value.bornAt == null || value.diedAt == null) {
      return true
    }

    val bornGreaterThanDied = value.bornAt!! > value.diedAt!!
    val finishLessThanStart = value.diedAt!! < value.bornAt!!

    if (!bornGreaterThanDied && !finishLessThanStart) {
      return true
    }

    context.disableDefaultConstraintViolation()
    context.buildConstraintViolationWithTemplate(if (bornGreaterThanDied) bornGreaterMessage else diedLessMessage)
      .addPropertyNode(if (bornGreaterThanDied) "bornAt" else "diedAt")
      .addConstraintViolation()

    return false
  }
}
