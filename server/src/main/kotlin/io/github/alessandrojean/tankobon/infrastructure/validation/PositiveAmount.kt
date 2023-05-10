package io.github.alessandrojean.tankobon.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import javax.money.MonetaryAmount
import kotlin.reflect.KClass

@Constraint(validatedBy = [MinAmountValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PositiveAmount(
  val message: String = "amount must be a greater or equal than zero",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)

class MinAmountValidator : ConstraintValidator<PositiveAmount, MonetaryAmount> {
  override fun isValid(value: MonetaryAmount, context: ConstraintValidatorContext): Boolean {
    return value.isPositiveOrZero
  }
}
