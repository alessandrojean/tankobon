package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class LibraryLifecycle(
  private val libraryRepository: LibraryRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  fun addLibrary(library: Library): Library {
    logger.info { "Adding new library: ${library.name}" }

    libraryRepository.insert(library)
    eventPublisher.publishEvent(DomainEvent.LibraryAdded(library))

    return libraryRepository.findById(library.id)
  }

  fun updateLibrary(toUpdate: Library) {
    logger.info { "Updating library: ${toUpdate.id}" }

    libraryRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.LibraryUpdated(toUpdate))
  }

  fun deleteLibrary(library: Library) {
    logger.info { "Deleting library: $library" }

    transactionTemplate.executeWithoutResult {
      libraryRepository.delete(library.id)
    }

    eventPublisher.publishEvent(DomainEvent.LibraryDeleted(library))
  }
}