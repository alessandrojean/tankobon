package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.ImageDetails
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class PublisherPictureLifecycle(
  properties: TankobonProperties,
  imageConverter: ImageConverter,
  eventPublisher: EventPublisher,
  private val publisherRepository: PublisherRepository,
) : EntityImageLifecycle("publisher", eventPublisher, imageConverter) {

  companion object {
    private const val PICTURES_DIR = "publishers"
  }

  override val imagesDirectoryPath: Path = Paths.get(properties.imagesDir, PICTURES_DIR)
  override val thumbnailSizes = arrayOf(64, 128, 256)

  override fun entityExistsById(entityId: String): Boolean {
    return publisherRepository.findByIdOrNull(entityId) != null
  }

  override fun createEvent(imageDetails: ImageDetails, type: PublishEventType) = when (type) {
    PublishEventType.CREATED -> DomainEvent.PublisherPictureAdded(imageDetails)
    PublishEventType.UPDATED -> DomainEvent.PublisherPictureUpdated(imageDetails)
    else -> DomainEvent.PublisherPictureDeleted(imageDetails)
  }

  override fun isDeleteEvent(event: DomainEvent) = event is DomainEvent.PublisherDeleted

  override fun getEntityIdFromDeleteEvent(event: DomainEvent) = (event as DomainEvent.PublisherDeleted).publisher.id

}