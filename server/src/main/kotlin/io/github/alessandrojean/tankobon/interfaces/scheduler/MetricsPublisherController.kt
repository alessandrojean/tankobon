package io.github.alessandrojean.tankobon.interfaces.scheduler

import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.image.BookCoverLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.UserAvatarLifecycle
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_EVENTS
import io.github.alessandrojean.tankobon.infrastructure.jms.TOPIC_FACTORY
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

const val METER_TASKS_EXECUTION = "tankobon.tasks.execution"
const val METER_TASKS_FAILURE = "tankobon.tasks.failure"

@Profile("!test")
@Component
class MetricsPublisherController(
  private val libraryRepository: LibraryRepository,
  private val bookRepository: BookRepository,
  private val userRepository: TankobonUserRepository,
  private val bookCoverLifecycle: BookCoverLifecycle,
  private val userAvatarLifecycle: UserAvatarLifecycle,
  private val meterRegistry: MeterRegistry,
) {

  companion object {
    private const val LIBRARIES = "libraries"
    private const val BOOKS = "books"
    private const val BOOK_COVERS = "books.covers"
    private const val USERS = "users"
    private const val USER_AVATARS = "users.avatars"
  }

  init {
    Timer.builder(METER_TASKS_EXECUTION)
      .description("Task execution time")
      .register(meterRegistry)

    Counter.builder(METER_TASKS_FAILURE)
      .description("Count of failed tasks")
      .register(meterRegistry)
  }

  private final val entitiesNoTags = listOf(LIBRARIES, BOOKS, BOOK_COVERS, USERS, USER_AVATARS)
  private final val allEntities = entitiesNoTags

  val noTagGauges = entitiesNoTags.associateWith { entity ->
    AtomicLong(0).also { value ->
      Gauge.builder("tankobon.$entity", value) { value.get().toDouble() }
        .description("The number of ${entity.replace(".", " ")}")
        .baseUnit("count")
        .register(meterRegistry)
    }
  }

  @JmsListener(destination = TOPIC_EVENTS, containerFactory = TOPIC_FACTORY)
  private fun pushMetricsOnEvent(event: DomainEvent) {
    when (event) {
      is DomainEvent.LibraryAdded -> noTagGauges[LIBRARIES]?.incrementAndGet()
      is DomainEvent.LibraryDeleted -> noTagGauges[LIBRARIES]?.decrementAndGet()

      is DomainEvent.BookAdded -> noTagGauges[BOOKS]?.incrementAndGet()
      is DomainEvent.BookDeleted -> noTagGauges[BOOKS]?.decrementAndGet()

      is DomainEvent.BookCoverAdded -> noTagGauges[BOOK_COVERS]?.incrementAndGet()
      is DomainEvent.BookCoverDeleted -> noTagGauges[BOOK_COVERS]?.decrementAndGet()

      is DomainEvent.UserAdded -> noTagGauges[USERS]?.incrementAndGet()
      is DomainEvent.UserDeleted -> noTagGauges[USERS]?.decrementAndGet()

      is DomainEvent.UserAvatarAdded -> noTagGauges[USER_AVATARS]?.incrementAndGet()
      is DomainEvent.UserAvatarDeleted -> noTagGauges[USER_AVATARS]?.decrementAndGet()

      else -> Unit
    }
  }

  @EventListener(ApplicationReadyEvent::class)
  fun pushAllMetrics() {
    allEntities.forEach { pushMetricsCount(it) }
  }

  private fun pushMetricsCount(entity: String) {
    when (entity) {
      LIBRARIES -> noTagGauges[LIBRARIES]?.set(libraryRepository.count())
      BOOKS -> noTagGauges[BOOKS]?.set(bookRepository.count())
      BOOK_COVERS -> noTagGauges[BOOK_COVERS]?.set(bookCoverLifecycle.count())
      USERS -> noTagGauges[USERS]?.set(userRepository.count())
      USER_AVATARS -> noTagGauges[USER_AVATARS]?.set(userAvatarLifecycle.count())
    }
  }

}