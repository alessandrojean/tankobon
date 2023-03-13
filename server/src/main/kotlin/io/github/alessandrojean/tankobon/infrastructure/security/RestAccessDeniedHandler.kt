package io.github.alessandrojean.tankobon.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component("delegatedAccessDeniedHandler")
class RestAccessDeniedHandler(
  @Autowired private val handlerExceptionResolver: HandlerExceptionResolver,
) : AccessDeniedHandler {

  override fun handle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    accessDeniedException: AccessDeniedException,
  ) {
    handlerExceptionResolver.resolveException(request, response, null, accessDeniedException)
  }
}