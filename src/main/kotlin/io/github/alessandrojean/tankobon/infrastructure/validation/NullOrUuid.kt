package io.github.alessandrojean.tankobon.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.constraints.Null
import org.hibernate.validator.constraints.CompositionType
import org.hibernate.validator.constraints.ConstraintComposition
import org.hibernate.validator.constraints.UUID
import kotlin.reflect.KClass

@ConstraintComposition(CompositionType.OR)
@Constraint(validatedBy = [])
@Null
@UUID(version = [4])
@Target(
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.FIELD,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.TYPE
)
@Retention(AnnotationRetention.RUNTIME)
annotation class NullOrUuid(
  val message: String = "Must be null or valid UUID v4",
  val groups: Array<KClass<out Any>> = [],
  val payload: Array<KClass<out Any>> = [],
)
