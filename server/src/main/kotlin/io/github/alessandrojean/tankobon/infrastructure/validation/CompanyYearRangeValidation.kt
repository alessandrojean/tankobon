package io.github.alessandrojean.tankobon.infrastructure.validation

import io.github.alessandrojean.tankobon.domain.model.DurationalCompanyYear
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [DurationalCompanyYearRangeValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CompanyYearRangeValidation(
  val message: String = "must contain a valid year range",
  val foundingGreaterMessage: String = "must be less or equal than the dissolution year",
  val dissolutionLessMessage: String = "must be greater or equal than the founding year",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class DurationalCompanyYearRangeValidator : ConstraintValidator<CompanyYearRangeValidation, DurationalCompanyYear> {
  private lateinit var foundingGreaterMessage: String
  private lateinit var dissolutionLessMessage: String

  override fun initialize(constraintAnnotation: CompanyYearRangeValidation) {
    foundingGreaterMessage = constraintAnnotation.foundingGreaterMessage
    dissolutionLessMessage = constraintAnnotation.dissolutionLessMessage
  }

  override fun isValid(value: DurationalCompanyYear, context: ConstraintValidatorContext): Boolean {
    if (value.foundingYear == null || value.dissolutionYear == null) {
      return true
    }

    val foundingGreaterThanDissolution = value.foundingYear!! > value.dissolutionYear!!
    val dissolutionLessThanFounding = value.dissolutionYear!! < value.foundingYear!!

    if (!foundingGreaterThanDissolution && !dissolutionLessThanFounding) {
      return true
    }

    context.disableDefaultConstraintViolation()
    context.buildConstraintViolationWithTemplate(if (foundingGreaterThanDissolution) foundingGreaterMessage else dissolutionLessMessage)
      .addPropertyNode(if (foundingGreaterThanDissolution) "foundingYear" else "dissolutionYear")
      .addConstraintViolation()

    return false
  }
}
