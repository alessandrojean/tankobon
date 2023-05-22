package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import io.github.alessandrojean.tankobon.domain.persistence.ReadProgressRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class ReadProgressLifecycle(
  private val readProgressRepository: ReadProgressRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  fun addReadProgress(readProgress: ReadProgress): ReadProgress {
    logger.info { "Adding new read progress: $readProgress" }

    readProgressRepository.insert(readProgress)
    eventPublisher.publishEvent(DomainEvent.ReadProgressAdded(readProgress))

    return readProgressRepository.findByIdOrNull(readProgress.id)!!
  }

  fun updateReadProgress(toUpdate: ReadProgress) {
    logger.info { "Updating read progress: $toUpdate" }

    readProgressRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.ReadProgressUpdated(toUpdate))
  }

  fun deleteReadProgress(readProgress: ReadProgress) {
    transactionTemplate.executeWithoutResult {
      readProgressRepository.delete(readProgress.id)
    }

    eventPublisher.publishEvent(DomainEvent.ReadProgressDeleted(readProgress))
  }
}
