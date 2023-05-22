package io.github.alessandrojean.tankobon.interfaces.scheduler

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Profile("!test & noclaim")
@Component
class InitialUserController(
  private val userLifecycle: TankobonUserLifecycle,
  private val initialUsers: List<TankobonUser>,
) {

  @EventListener(ApplicationReadyEvent::class)
  fun createInitialUserOnStartupIfNoneExist() {
    if (userLifecycle.countUsers() == 0L) {
      logger.info { "No users exist in database, creating initial users" }

      initialUsers.forEach {
        userLifecycle.createUser(it)
        logger.info { "Initial user created. Login: ${it.email}, Password: ${it.password}" }
      }
    }
  }
}

@Configuration
@Profile("dev")
class InitialUsersDevConfiguration {
  @Bean
  fun initialUsers(): List<TankobonUser> = listOf(
    TankobonUser("admin@example.org", "admin", isAdmin = true, "Administrator"),
    TankobonUser("user@example.org", "user", isAdmin = false, "User"),
  )
}

@Configuration
@Profile("!dev")
class InitialUsersProdConfiguration {
  @Bean
  fun initialUsers(): List<TankobonUser> = listOf(
    TankobonUser(
      email = "admin@example.org",
      password = RandomStringUtils.randomAlphanumeric(12),
      isAdmin = true,
      name = "Administrator",
    ),
  )
}
