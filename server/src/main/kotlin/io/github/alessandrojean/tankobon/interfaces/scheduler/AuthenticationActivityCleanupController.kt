package io.github.alessandrojean.tankobon.interfaces.scheduler

import io.github.alessandrojean.tankobon.domain.persistence.AuthenticationActivityRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Component
class AuthenticationActivityCleanupController(
  private val authenticationActivityRepository: AuthenticationActivityRepository,
) {

  // Run every day
  @Scheduled(fixedRate = 86_400_000)
  fun cleanup() {
    val olderThan = LocalDateTime.now(ZoneId.of("Z")).minusMonths(1)
    logger.info { "Removing authentication activity older than $olderThan (UTC)" }
    authenticationActivityRepository.deleteOlderThan(olderThan)
  }
}
