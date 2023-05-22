package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class CollectionLifecycle(
  private val collectionRepository: CollectionRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addCollection(collection: Collection): Collection {
    logger.info { "Adding new collection: $collection" }

    if (collectionRepository.existsByNameInLibrary(collection.name, collection.libraryId)) {
      throw DuplicateNameException("Collection name already exists in the library specified")
    }

    collectionRepository.insert(collection)
    eventPublisher.publishEvent(DomainEvent.CollectionAdded(collection))

    return collectionRepository.findByIdOrNull(collection.id)!!
  }

  fun updateCollection(toUpdate: Collection) {
    logger.info { "Updating collection: $toUpdate" }

    val existing = collectionRepository.findByIdOrNull(toUpdate.id)
      ?: throw IdDoesNotExistException("Cannot update collection that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      collectionRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Collection name already exists in the library specified")
    }

    collectionRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.CollectionUpdated(toUpdate))
  }

  fun deleteCollection(collection: Collection) {
    transactionTemplate.executeWithoutResult {
      collectionRepository.delete(collection.id)
    }

    eventPublisher.publishEvent(DomainEvent.CollectionDeleted(collection))
  }
}
