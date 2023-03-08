package io.github.alessandrojean.tankobon.infrastructure.security

import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ErrorResponseDto
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class RestExceptionHandler {

  @ExceptionHandler(AuthenticationException::class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ResponseBody
  fun onAuthenticationException(e: AuthenticationException) = ErrorResponseDto(
    errors = listOf(
      ErrorDto(
        id = e.javaClass.simpleName,
        status = HttpStatus.UNAUTHORIZED.value(),
        title = e.localizedMessage.orEmpty()
          .ifEmpty { e.message.orEmpty() },
        details = "Check if the credentials were sent properly",
      )
    )
  )

  @ExceptionHandler(AccessDeniedException::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  fun onAccessDeniedException(e: AccessDeniedException) = ErrorResponseDto(
    errors = listOf(
      ErrorDto(
        id = e.javaClass.simpleName,
        status = HttpStatus.FORBIDDEN.value(),
        title = e.localizedMessage.orEmpty()
          .ifEmpty { e.message.orEmpty() },
        details = "You don't have access to this operation",
      )
    )
  )
}
