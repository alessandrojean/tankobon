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
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorResponseDto
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@ControllerAdvice(annotations = [RestController::class])
class ErrorHandlingControllerAdvice {

  @ExceptionHandler(ConstraintViolationException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onConstraintViolationException(e: ConstraintViolationException): ErrorResponseDto {
    return ErrorResponseDto(
      e.constraintViolations.map {
        ErrorDto(
          id = it.javaClass.simpleName,
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
          id = it.javaClass.simpleName,
          status = HttpStatus.BAD_REQUEST.value(),
          title = it.field,
          details = it.defaultMessage.orEmpty(),
        )
      }
    )
  }

  @ExceptionHandler(DuplicateNameException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun onDuplicateNameException(e: DuplicateNameException) =
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

  private fun CodedException.toErrorResponseDto(status: HttpStatus = HttpStatus.BAD_REQUEST) = ErrorResponseDto(
    errors = listOf(toErrorDto(status))
  )

  private fun CodedException.toErrorDto(status: HttpStatus = HttpStatus.BAD_REQUEST) = ErrorDto(
    id = javaClass.simpleName,
    status = status.value(),
    title = message.orEmpty(),
    details = code,
  )
}
