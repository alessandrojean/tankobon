package io.github.alessandrojean.tankobon.interfaces.api.rest

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ErrorHandlingControllerAdvice {

  @ExceptionHandler(ConstraintViolationException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onConstraintViolationException(e: ConstraintViolationException): ValidationErrorResponse {
    return ValidationErrorResponse(
      e.constraintViolations.map {
        Violation(it.propertyPath.toString(), it.message)
      }
    )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onMethodArgumentNotValidException(e: MethodArgumentNotValidException): ValidationErrorResponse {
    return ValidationErrorResponse(
      e.bindingResult.fieldErrors.map {
        Violation(it.field, it.defaultMessage)
      }
    )
  }
}

data class ValidationErrorResponse(
  val violations: List<Violation> = emptyList(),
)

data class Violation(
  val fieldName: String? = null,
  val message: String? = null,
)