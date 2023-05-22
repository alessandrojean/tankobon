package io.github.alessandrojean.tankobon.infrastructure.security

import io.github.alessandrojean.tankobon.domain.model.AuthenticationActivity
import io.github.alessandrojean.tankobon.domain.persistence.AuthenticationActivityRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.util.EventObject

private val logger = KotlinLogging.logger {}

@Component
class LoginListener(
  private val authenticationActivityRepository: AuthenticationActivityRepository,
  private val userRepository: TankobonUserRepository,
) {

  @EventListener
  fun onSuccess(event: AuthenticationSuccessEvent) {
    val user = (event.authentication.principal as TankobonPrincipal).user
    val source = when (event.source) {
      is UsernamePasswordAuthenticationToken -> "Password"
      else -> null
    }

    val activity = AuthenticationActivity(
      userId = user.id,
      email = user.email,
      ip = event.getIp(),
      userAgent = event.getUserAgent(),
      success = true,
      source = source,
    )

    logger.info { activity }
    authenticationActivityRepository.insert(activity)
  }

  @EventListener
  fun onFailure(event: AbstractAuthenticationFailureEvent) {
    val user = event.authentication?.principal?.toString().orEmpty()
    val source = when (event.source) {
      is UsernamePasswordAuthenticationToken -> "Password"
      else -> null
    }

    val activity = AuthenticationActivity(
      userId = userRepository.findByEmailIgnoreCaseOrNull(user)?.id,
      email = user,
      ip = event.getIp(),
      userAgent = event.getUserAgent(),
      success = false,
      source = source,
      error = event.exception.javaClass.simpleName.toErrorId(),
    )

    logger.info { activity }
    authenticationActivityRepository.insert(activity)
  }

  private fun EventObject.getIp(): String? = try {
    when (source) {
      is WebAuthenticationDetails -> (source as WebAuthenticationDetails).remoteAddress
      is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as WebAuthenticationDetails).remoteAddress
      else -> null
    }
  } catch (e: Exception) {
    null
  }

  private fun EventObject.getUserAgent(): String? = try {
    when (source) {
      is UserAgentWebAuthenticationDetails -> (source as UserAgentWebAuthenticationDetails).userAgent
      is AbstractAuthenticationToken -> ((source as AbstractAuthenticationToken).details as UserAgentWebAuthenticationDetails).userAgent
      else -> null
    }
  } catch (e: Exception) {
    null
  }

  private fun String.toErrorId(): String =
    replace(EXCEPTION_REGEX, "")
      .replace(CAMEL_CASE_REGEX, "$1_$2")
      .uppercase()

  companion object {
    private val CAMEL_CASE_REGEX = "([a-z])([A-Z]+)".toRegex()
    private val EXCEPTION_REGEX = "(Exception|Impl)$".toRegex()
  }
}
