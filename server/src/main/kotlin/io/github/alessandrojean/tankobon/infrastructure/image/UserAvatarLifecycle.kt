package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.Image
import io.github.alessandrojean.tankobon.domain.persistence.ImageRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class UserAvatarLifecycle(
  properties: TankobonProperties,
  imageConverter: ImageConverter,
  eventPublisher: EventPublisher,
  imageRepository: ImageRepository,
  private val userRepository: TankobonUserRepository,
) : EntityImageLifecycle("user", eventPublisher, imageConverter, imageRepository) {

  companion object {
    private const val AVATARS_DIR = "avatars"
  }

  override val imagesDirectoryPath: Path = Paths.get(properties.imagesDir, AVATARS_DIR)
  override val thumbnailSizes = arrayOf(64, 128, 256)

  override fun entityExistsById(entityId: String): Boolean {
    return userRepository.findByIdOrNull(entityId) != null
  }

  override fun createEvent(imageDetails: Image, type: PublishEventType) = when (type) {
    PublishEventType.CREATED -> DomainEvent.UserAvatarAdded(imageDetails)
    PublishEventType.UPDATED -> DomainEvent.UserAvatarUpdated(imageDetails)
    else -> DomainEvent.UserAvatarDeleted(imageDetails)
  }

  override fun isDeleteEvent(event: DomainEvent) = event is DomainEvent.UserDeleted

  override fun getEntityIdFromDeleteEvent(event: DomainEvent) = (event as DomainEvent.UserDeleted).user.id

}