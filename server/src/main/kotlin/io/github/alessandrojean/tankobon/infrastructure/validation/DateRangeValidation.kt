package io.github.alessandrojean.tankobon.infrastructure.validation

import io.github.alessandrojean.tankobon.domain.model.Durational
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [DurationalDateRangeValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DateRangeValidation(
  val message: String = "must contain a valid date range",
  val startGreaterMessage: String = "must be less or equal than the finish date",
  val finishLessMessage: String = "must be greater or equal than the start date",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class DurationalDateRangeValidator : ConstraintValidator<DateRangeValidation, Durational> {
  private lateinit var startGreaterMessage: String
  private lateinit var finishLessMessage: String

  override fun initialize(constraintAnnotation: DateRangeValidation) {
    startGreaterMessage = constraintAnnotation.startGreaterMessage
    finishLessMessage = constraintAnnotation.finishLessMessage
  }

  override fun isValid(value: Durational, context: ConstraintValidatorContext): Boolean {
    if (value.startedAt == null || value.finishedAt == null) {
      return true
    }

    val startGreaterThanFinish = value.startedAt!! > value.finishedAt!!
    val finishLessThanStart = value.finishedAt!! < value.startedAt!!

    if (!startGreaterThanFinish && !finishLessThanStart) {
      return true
    }

    context.disableDefaultConstraintViolation()
    context.buildConstraintViolationWithTemplate(if (startGreaterThanFinish) startGreaterMessage else finishLessMessage)
      .addPropertyNode(if (startGreaterThanFinish) "startedAt" else "finishedAt")
      .addConstraintViolation()

    return false
  }
}
