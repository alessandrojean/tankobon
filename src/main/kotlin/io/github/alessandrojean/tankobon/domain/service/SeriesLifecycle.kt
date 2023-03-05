package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateNameException
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class SeriesLifecycle(
  private val seriesRepository: SeriesRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateNameException::class)
  fun addSeries(series: Series): Series {
    logger.info { "Adding new series: $series" }

    if (seriesRepository.existsByNameInLibrary(series.name, series.libraryId)) {
      throw DuplicateNameException("Series name already exists in the library specified")
    }

    seriesRepository.insert(series)
    eventPublisher.publishEvent(DomainEvent.SeriesAdded(series))

    return seriesRepository.findByIdOrNull(series.id)!!
  }

  fun updateSeries(toUpdate: Series) {
    logger.info { "Updating series: $toUpdate" }

    val existing = seriesRepository.findByIdOrNull(toUpdate.id)
      ?: throw IllegalArgumentException("Cannot update series that does not exist")

    if (
      !existing.name.equals(toUpdate.name, true) &&
      seriesRepository.existsByNameInLibrary(toUpdate.name, toUpdate.libraryId)
    ) {
      throw DuplicateNameException("Series name already exists in the library specified")
    }

    seriesRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.SeriesUpdated(toUpdate))
  }

  fun deleteSeries(series: Series) {
    transactionTemplate.executeWithoutResult {
      seriesRepository.delete(series.id)
    }

    eventPublisher.publishEvent(DomainEvent.SeriesDeleted(series))
  }
}