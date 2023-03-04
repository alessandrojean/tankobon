package io.github.alessandrojean.tankobon.infrastructure.security

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.ROLE_USER
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
  private val sessionCookieName: String,
  private val userAgentWebAuthenticationDetailsSource: WebAuthenticationDetailsSource,
  private val sessionRegistry: SessionRegistry,
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
            "/set-cookie",
            "/error**",
            "/css/**",
            "/img/**",
            "/js/**",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/",
            "/index.html",
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
      }
      .logout {
        it.logoutUrl("/api/logout")
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

    return http.build()
  }
}