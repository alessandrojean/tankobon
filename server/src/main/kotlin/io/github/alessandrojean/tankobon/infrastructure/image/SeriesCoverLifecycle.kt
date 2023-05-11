package io.github.alessandrojean.tankobon.infrastructure.image

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.ImageDetails
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.nio.file.Paths

@Component
class SeriesCoverLifecycle(
  properties: TankobonProperties,
  imageConverter: ImageConverter,
  eventPublisher: EventPublisher,
  private val seriesRepository: SeriesRepository,
) : EntityImageLifecycle("series", eventPublisher, imageConverter) {

  companion object {
    private const val PICTURES_DIR = "series"
  }

  override val imagesDirectoryPath: Path = Paths.get(properties.imagesDir, PICTURES_DIR)
  override val thumbnailSizes = arrayOf(128, 256, 512)

  override fun entityExistsById(entityId: String): Boolean {
    return seriesRepository.findByIdOrNull(entityId) != null
  }

  override fun createEvent(imageDetails: ImageDetails, type: PublishEventType) = when (type) {
    PublishEventType.CREATED -> DomainEvent.SeriesCoverAdded(imageDetails)
    PublishEventType.UPDATED -> DomainEvent.SeriesCoverUpdated(imageDetails)
    else -> DomainEvent.SeriesCoverDeleted(imageDetails)
  }

  override fun isDeleteEvent(event: DomainEvent) = event is DomainEvent.SeriesDeleted

  override fun getEntityIdFromDeleteEvent(event: DomainEvent) = (event as DomainEvent.SeriesDeleted).series.id

}