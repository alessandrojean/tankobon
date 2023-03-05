package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class PublisherLifecycle(
  private val publisherRepository: PublisherRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addPublisher(publisher: Publisher): Publisher {
    logger.info { "Adding new publisher: $publisher" }

    if (publisherRepository.existsByNameInLibrary(publisher.name, publisher.libraryId)) {
      throw DuplicateNameException("Publisher name already exists in the library specified")
    }

    publisherRepository.insert(publisher)
    eventPublisher.publishEvent(DomainEvent.PublisherAdded(publisher))

    return publisherRepository.findByIdOrNull(publisher.id)!!
  }

  fun updatePublisher(toUpdate: Publisher) {
    logger.info { "Updating publisher: $toUpdate" }

    val existing = publisherRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update publisher that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      publisherRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Publisher name already exists in the library specified")
    }

    publisherRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.PublisherUpdated(toUpdate))
  }

  fun deletePublisher(publisher: Publisher) {
    transactionTemplate.executeWithoutResult {
      publisherRepository.delete(publisher.id)
    }

    eventPublisher.publishEvent(DomainEvent.PublisherDeleted(publisher))
  }
}