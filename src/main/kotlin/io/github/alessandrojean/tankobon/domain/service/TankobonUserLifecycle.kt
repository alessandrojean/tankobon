package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.UserEmailAlreadyExistsException
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import mu.KotlinLogging
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class TankobonUserLifecycle(
  private val userRepository: TankobonUserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val sessionRegistry: SessionRegistry,
  private val transactionTemplate: TransactionTemplate,
  private val eventPublisher: EventPublisher
) {

  fun updatePassword(user: TankobonUser, newPassword: String, expireSessions: Boolean) {
    logger.info { "Changing password for user ${user.email}" }
    val updatedUser = user.copy(password = passwordEncoder.encode(newPassword))
    userRepository.update(updatedUser)

    if (expireSessions) {
      expireSessions(updatedUser)
    }

    eventPublisher.publishEvent(DomainEvent.UserUpdated(updatedUser))
  }

  fun updateUser(user: TankobonUser) {
    val existing = userRepository.findByIdOrNull(user.id)
    requireNotNull(existing) { "User doesn't exist, cannot update: $user" }

    val toUpdate = user.copy(password = existing.password)
    logger.info { "Update user: $toUpdate" }
    userRepository.update(toUpdate)

    val expireSessions = existing.isAdmin != user.isAdmin

    if (expireSessions) {
      expireSessions(toUpdate)
    }

    eventPublisher.publishEvent(DomainEvent.UserUpdated(toUpdate))
  }

  fun countUsers() = userRepository.count()

  @Throws(UserEmailAlreadyExistsException::class)
  fun createUser(tankobonUser: TankobonUser): TankobonUser {
    if (userRepository.existsByEmailIgnoreCase(tankobonUser.email)) {
      throw UserEmailAlreadyExistsException("A user with the same email already exists: ${tankobonUser.email}")
    }

    userRepository.insert(tankobonUser.copy(password = passwordEncoder.encode(tankobonUser.password)))

    val createdUser = userRepository.findByIdOrNull(tankobonUser.id)!!
    logger.info { "User created: $createdUser" }

    return createdUser
  }

  fun deleteUser(user: TankobonUser) {
    logger.info { "Deleting user: $user" }

    transactionTemplate.executeWithoutResult {
      userRepository.delete(user.id)
    }

    expireSessions(user)

    eventPublisher.publishEvent(DomainEvent.UserUpdated(user))
  }

  fun expireSessions(user: TankobonUser) {
    logger.info { "Expiring all sessions for user: ${user.email}" }
    sessionRegistry
      .getAllSessions(TankobonPrincipal(user), false)
      .forEach {
        logger.info { "Expiring session: ${it.sessionId}" }
        it.expireNow()
      }
  }

}