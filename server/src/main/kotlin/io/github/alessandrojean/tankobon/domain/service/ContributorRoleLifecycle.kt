package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class ContributorRoleLifecycle(
  private val contributorRoleRepository: ContributorRoleRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addContributorRole(contributorRole: ContributorRole): ContributorRole {
    logger.info { "Adding new contributor role: $contributorRole" }

    if (contributorRoleRepository.existsByNameInLibrary(contributorRole.name, contributorRole.libraryId)) {
      throw DuplicateNameException("Contributor role name already exists in the library specified")
    }

    contributorRoleRepository.insert(contributorRole)
    eventPublisher.publishEvent(DomainEvent.ContributorRoleAdded(contributorRole))

    return contributorRoleRepository.findByIdOrNull(contributorRole.id)!!
  }

  fun updateContributorRole(toUpdate: ContributorRole) {
    logger.info { "Updating contributor role: $toUpdate" }

    val existing = contributorRoleRepository.findByIdOrNull(toUpdate.id)
      ?: throw IdDoesNotExistException("Cannot update contributor role that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      contributorRoleRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Contributor role name already exists in the library specified")
    }

    contributorRoleRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.ContributorRoleUpdated(toUpdate))
  }

  fun deleteContributorRole(contributorRole: ContributorRole) {
    transactionTemplate.executeWithoutResult {
      contributorRoleRepository.delete(contributorRole.id)
    }

    eventPublisher.publishEvent(DomainEvent.ContributorRoleDeleted(contributorRole))
  }
}
