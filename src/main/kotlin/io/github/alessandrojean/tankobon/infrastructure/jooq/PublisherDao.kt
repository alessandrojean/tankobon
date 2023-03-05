package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.PublisherRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.PUBLISHER as TablePublisher

@Component
class PublisherDao(
  private val dsl: DSLContext,
) : PublisherRepository {

  override fun findById(publisherId: String): Publisher = findByIdOrNull(publisherId)!!

  override fun findByIdOrNull(publisherId: String): Publisher? =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.ID.eq(publisherId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Publisher> =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.LIBRARY_ID.eq(libraryId))
      .fetchInto(TablePublisher)
      .map { it.toDomain() }

  override fun findAll(): Collection<Publisher> =
    dsl.selectFrom(TablePublisher)
      .fetchInto(TablePublisher)
      .map { it.toDomain() }

  override fun findAllByIds(publisherIds: Collection<String>): Collection<Publisher> =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.ID.`in`(publisherIds))
      .fetchInto(TablePublisher)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TablePublisher)
        .where(TablePublisher.NAME.equalIgnoreCase(name))
        .and(TablePublisher.LIBRARY_ID.eq(libraryId))
    )

  @Transactional
  override fun insert(publisher: Publisher) {
    dsl.insertInto(TablePublisher)
      .set(TablePublisher.ID, publisher.id)
      .set(TablePublisher.NAME, publisher.name)
      .set(TablePublisher.DESCRIPTION, publisher.description)
      .set(TablePublisher.LIBRARY_ID, publisher.libraryId)
      .execute()
  }

  @Transactional
  override fun update(publisher: Publisher) {
    dsl.update(TablePublisher)
      .set(TablePublisher.ID, publisher.id)
      .set(TablePublisher.NAME, publisher.name)
      .set(TablePublisher.DESCRIPTION, publisher.description)
      .set(TablePublisher.LIBRARY_ID, publisher.libraryId)
      .set(TablePublisher.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TablePublisher.ID.eq(publisher.id))
      .execute()
  }

  @Transactional
  override fun delete(publisherId: String) {
    dsl.deleteFrom(TablePublisher)
      .where(TablePublisher.ID.eq(publisherId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TablePublisher).execute()
  }

  override fun count(): Long = dsl.fetchCount(TablePublisher).toLong()

  private fun PublisherRecord.toDomain(): Publisher = Publisher(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}