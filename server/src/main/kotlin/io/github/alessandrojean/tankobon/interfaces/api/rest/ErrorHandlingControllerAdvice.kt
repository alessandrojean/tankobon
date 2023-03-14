package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.CantChangePasswordInDemoModeException
import io.github.alessandrojean.tankobon.domain.model.CodedException
import io.github.alessandrojean.tankobon.domain.model.DuplicateCodeException
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.LibraryOwnerChangedException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIsNotFromSameLibraryException
import io.github.alessandrojean.tankobon.domain.model.ServerAlreadyClaimedException
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.model.UserEmailAlreadyExistsException
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorResponseDto
import jakarta.validation.ConstraintViolationException
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MaxUploadSizeExceededException

@ControllerAdvice(annotations = [RestController::class])
class ErrorHandlingControllerAdvice(
  private val environment: Environment,
) {

  private val showStackTrace by lazy {
    environment.activeProfiles.contains("dev") ||
      environment.activeProfiles.contains("test")
  }

  @ExceptionHandler(ConstraintViolationException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onConstraintViolationException(e: ConstraintViolationException): ErrorResponseDto {
    return ErrorResponseDto(
      e.constraintViolations.map {
        ErrorDto(
          id = it.javaClass.simpleName.toErrorId(),
          status = HttpStatus.BAD_REQUEST.value(),
          title = it.propertyPath.toString(),
          details = it.message,
        )
      }
    )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onMethodArgumentNotValidException(e: MethodArgumentNotValidException): ErrorResponseDto {
    return ErrorResponseDto(
      e.bindingResult.fieldErrors.map {
        ErrorDto(
          id = it.javaClass.simpleName.toErrorId(),
          status = HttpStatus.BAD_REQUEST.value(),
          title = it.field,
          details = it.defaultMessage.orEmpty(),
        )
      }
    )
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onHttpMessageNotReadableException(e: HttpMessageNotReadableException) =
    e.toErrorResponseDto(HttpStatus.BAD_REQUEST)

  @ExceptionHandler(DuplicateNameException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onDuplicateNameException(e: DuplicateNameException) =
    e.toErrorResponseDto()

  @ExceptionHandler(UserEmailAlreadyExistsException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onUserEmailAlreadyExistsException(e: UserEmailAlreadyExistsException) =
    e.toErrorResponseDto()

  @ExceptionHandler(DuplicateCodeException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onDuplicateCodeException(e: DuplicateCodeException) =
    e.toErrorResponseDto()

  @ExceptionHandler(RelationIdDoesNotExistException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onRelationIdDoesNotExistException(e: RelationIdDoesNotExistException) =
    e.toErrorResponseDto()

  @ExceptionHandler(RelationIsNotFromSameLibraryException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onRelationIsNotFromSameLibraryException(e: RelationIsNotFromSameLibraryException) =
    e.toErrorResponseDto()

  @ExceptionHandler(ServerAlreadyClaimedException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onServerAlreadyClaimedException(e: ServerAlreadyClaimedException) =
    e.toErrorResponseDto()

  @ExceptionHandler(MaxUploadSizeExceededException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onMaxUploadSizeExceededException(e: MaxUploadSizeExceededException) =
    e.toErrorResponseDto(HttpStatus.BAD_REQUEST)

  @ExceptionHandler(UserDoesNotHaveAccessException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  fun onUserDoesNotHaveAccessException(e: UserDoesNotHaveAccessException) =
    e.toErrorResponseDto(HttpStatus.FORBIDDEN)

  @ExceptionHandler(IdDoesNotExistException::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  fun onIdDoesNotExistException(e: IdDoesNotExistException) =
    e.toErrorResponseDto(HttpStatus.NOT_FOUND)

  @ExceptionHandler(CantChangePasswordInDemoModeException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  fun onCantChangePasswordInDemoModeException(e: CantChangePasswordInDemoModeException) =
    e.toErrorResponseDto(HttpStatus.FORBIDDEN)

  @ExceptionHandler(LibraryOwnerChangedException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  fun onLibraryOwnerChangedException(e: LibraryOwnerChangedException) =
    e.toErrorResponseDto(HttpStatus.FORBIDDEN)

  @ExceptionHandler(Exception::class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  fun onException(e: Exception) = e.toErrorResponseDto()

  private fun CodedException.toErrorResponseDto(status: HttpStatus = HttpStatus.BAD_REQUEST) = ErrorResponseDto(
    errors = listOf(toErrorDto(status))
  )

  private fun CodedException.toErrorDto(status: HttpStatus = HttpStatus.BAD_REQUEST) = ErrorDto(
    id = javaClass.simpleName.toErrorId(),
    status = status.value(),
    title = localizedMessage.orEmpty().ifEmpty { message.orEmpty() },
    details = code.ifEmpty { message.orEmpty() },
    stackTrace = stackTraceToString().takeIf { showStackTrace }
  )

  private fun Exception.toErrorResponseDto(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) = ErrorResponseDto(
    errors = listOf(
      ErrorDto(
        id = javaClass.simpleName.toErrorId(),
        status = status.value(),
        title = localizedMessage.orEmpty().ifEmpty { message.orEmpty() },
        details = message.orEmpty(),
        stackTrace = stackTraceToString().takeIf { showStackTrace },
      ),
    )
  )

  private fun String.toErrorId(): String =
    replace(EXCEPTION_REGEX, "")
      .replace(CAMEL_CASE_REGEX, "$1_$2")
      .uppercase()

  companion object {
    private val CAMEL_CASE_REGEX = "([a-z])([A-Z]+)".toRegex()
    private val EXCEPTION_REGEX = "(Exception|Impl)$".toRegex()
  }
}
