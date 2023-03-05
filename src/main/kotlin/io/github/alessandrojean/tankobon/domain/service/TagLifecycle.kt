package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class TagLifecycle(
  private val tagRepository: TagRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addTag(tag: Tag): Tag {
    logger.info { "Adding new tag: $tag" }

    if (tagRepository.existsByNameInLibrary(tag.name, tag.libraryId)) {
      throw DuplicateNameException("Tag name already exists in the library specified")
    }

    tagRepository.insert(tag)
    eventPublisher.publishEvent(DomainEvent.TagAdded(tag))

    return tagRepository.findByIdOrNull(tag.id)!!
  }

  fun updateTag(toUpdate: Tag) {
    logger.info { "Updating tag: $toUpdate" }

    val existing = tagRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update tag that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      tagRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Tag name already exists in the library specified")
    }

    tagRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.TagUpdated(toUpdate))
  }

  fun deleteTag(tag: Tag) {
    transactionTemplate.executeWithoutResult {
      tagRepository.delete(tag.id)
    }

    eventPublisher.publishEvent(DomainEvent.TagDeleted(tag))
  }
}