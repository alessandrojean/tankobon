package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(
  factory = WithMockCustomUserSecurityContextFactory::class,
  setupBefore = TestExecutionEvent.TEST_EXECUTION
)
annotation class WithMockCustomUser(
  val email: String = "user@example.org",
  val roles: Array<String> = [],
  val id: String = "0",
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
  override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
    val context = SecurityContextHolder.createEmptyContext()

    val principal = TankobonPrincipal(
      TankobonUser(
        email = customUser.email,
        password = "",
        isAdmin = customUser.roles.contains(ROLE_ADMIN),
        id = customUser.id,
      )
    )

    val auth = UsernamePasswordAuthenticationToken(principal, "", principal.authorities)
    context.authentication = auth

    return context
  }
}
