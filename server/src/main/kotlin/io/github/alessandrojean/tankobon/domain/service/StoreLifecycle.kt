package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class StoreLifecycle(
  private val storeRepository: StoreRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addStore(store: Store): Store {
    logger.info { "Adding new store: $store" }

    if (storeRepository.existsByNameInLibrary(store.name, store.libraryId)) {
      throw DuplicateNameException("Store name already exists in the library specified")
    }

    storeRepository.insert(store)
    eventPublisher.publishEvent(DomainEvent.StoreAdded(store))

    return storeRepository.findByIdOrNull(store.id)!!
  }

  fun updateStore(toUpdate: Store) {
    logger.info { "Updating store: $toUpdate" }

    val existing = storeRepository.findByIdOrNull(toUpdate.id)
      ?: throw IdDoesNotExistException("Cannot update store that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      storeRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Store name already exists in the library specified")
    }

    storeRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.StoreUpdated(toUpdate))
  }

  fun deleteStore(store: Store) {
    transactionTemplate.executeWithoutResult {
      storeRepository.delete(store.id)
    }

    eventPublisher.publishEvent(DomainEvent.StoreDeleted(store))
  }
}