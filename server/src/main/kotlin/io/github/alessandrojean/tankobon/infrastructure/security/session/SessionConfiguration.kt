package io.github.alessandrojean.tankobon.infrastructure.security.session

import com.github.gotson.spring.session.caffeine.CaffeineIndexedSessionRepository
import com.github.gotson.spring.session.caffeine.config.annotation.web.http.EnableCaffeineHttpSession
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.session.SessionRegistry
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.session.web.http.HttpSessionIdResolver

@EnableCaffeineHttpSession
@Configuration
class SessionConfiguration {

  @Bean
  fun sessionCookieName() = "SESSION"

  @Bean
  fun sessionHeaderName() = "X-Auth-Token"

  @Bean
  fun cookieSerializer(sessionCookieName: String): CookieSerializer =
    DefaultCookieSerializer().apply {setCookieName(sessionCookieName) }

  @Bean
  fun httpSessionIdResolver(sessionHeaderName: String, cookieSerializer: CookieSerializer): HttpSessionIdResolver =
    SmartHttpSessionIdResolver(sessionHeaderName, cookieSerializer)

  @Bean
  fun customizeSessionRepository(tankobonProperties: TankobonProperties) =
    SessionRepositoryCustomizer<CaffeineIndexedSessionRepository> {
      it.setDefaultMaxInactiveInterval(tankobonProperties.sessionTimeout.seconds.toInt())
    }

  @Bean
  fun sessionRegistry(sessionRepository: FindByIndexNameSessionRepository<*>): SessionRegistry =
    SpringSessionBackedSessionRegistry(sessionRepository)
}