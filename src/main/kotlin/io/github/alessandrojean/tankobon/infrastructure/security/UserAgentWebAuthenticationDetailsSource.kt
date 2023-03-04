package io.github.alessandrojean.tankobon.infrastructure.security

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest

@Component
class UserAgentWebAuthenticationDetailsSource : WebAuthenticationDetailsSource() {
  override fun buildDetails(context: HttpServletRequest): UserAgentWebAuthenticationDetails =
    UserAgentWebAuthenticationDetails(context)
}
