package io.github.alessandrojean.tankobon.infrastructure.security

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.ROLE_USER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val sessionCookieName: String,
  private val userAgentWebAuthenticationDetailsSource: WebAuthenticationDetailsSource,
  private val sessionRegistry: SessionRegistry,
  @Autowired
  @Qualifier("delegatedAuthenticationEntryPoint")
  private val authenticationEntryPoint: AuthenticationEntryPoint,
  @Autowired
  @Qualifier("delegatedAccessDeniedHandler")
  private val accessDeniedHandler: AccessDeniedHandler,
) {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .cors {}
      .csrf { it.disable() }
      .authorizeHttpRequests { auth ->
        auth.requestMatchers(EndpointRequest.to(HealthEndpoint::class.java)).permitAll()
        auth.requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(ROLE_ADMIN)

        auth
          .requestMatchers(
            "/api/v1/system-status",
            "/api/v1/claim",
            "/api",
            "/api/api-docs/**",
            "/images/**",
            "/set-cookie",
            "/error**",
            "/css/**",
            "/img/**",
            "/js/**",
            "/favicon.svg",
            "/",
            "/index.html",
            "/fonts/**",
            "/assets/**",
            "/flags/**",
          )
          .permitAll()

        auth
          .requestMatchers("/api/**")
          .hasRole(ROLE_USER)
      }
      .headers {
        it.cacheControl().disable()
      }
      .httpBasic {
        it.authenticationDetailsSource(userAgentWebAuthenticationDetailsSource)
        it.authenticationEntryPoint(authenticationEntryPoint)
      }
      .logout {
        it.logoutUrl("/api/v1/sign-out")
        it.deleteCookies(sessionCookieName)
        it.invalidateHttpSession(true)
      }
      .sessionManagement { session ->
        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        session.sessionConcurrency {
          it.sessionRegistry(sessionRegistry)
          it.maximumSessions(-1)
        }
      }
      .exceptionHandling {
        it.authenticationEntryPoint(authenticationEntryPoint)
        it.accessDeniedHandler(accessDeniedHandler)
      }

    return http.build()
  }
}
