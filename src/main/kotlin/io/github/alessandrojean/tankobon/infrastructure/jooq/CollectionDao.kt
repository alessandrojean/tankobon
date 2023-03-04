package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.CollectionRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.collections.Collection
import io.github.alessandrojean.tankobon.domain.model.Collection as DomainCollection
import io.github.alessandrojean.tankobon.jooq.Tables.COLLECTION as TableCollection

@Component
class CollectionDao(
  private val dsl: DSLContext,
) : CollectionRepository {

  override fun findById(collectionId: String): DomainCollection = findByIdOrNull(collectionId)!!

  override fun findByIdOrNull(collectionId: String): DomainCollection? =
    dsl.selectFrom(TableCollection)
      .where(TableCollection.ID.eq(collectionId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<DomainCollection> =
    dsl.selectFrom(TableCollection)
      .where(TableCollection.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableCollection)
      .map { it.toDomain() }

  override fun findAll(): Collection<DomainCollection> =
    dsl.selectFrom(TableCollection)
      .fetchInto(TableCollection)
      .map { it.toDomain() }

  override fun findAllByIds(collectionIds: Collection<String>): Collection<DomainCollection> =
    dsl.selectFrom(TableCollection)
      .where(TableCollection.ID.`in`(collectionIds))
      .fetchInto(TableCollection)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableCollection)
        .where(TableCollection.NAME.equalIgnoreCase(name))
        .and(TableCollection.LIBRARY_ID.eq(libraryId))
    )

  @Transactional
  override fun insert(collection: DomainCollection) {
    dsl.insertInto(TableCollection)
      .set(TableCollection.ID, collection.id)
      .set(TableCollection.NAME, collection.name)
      .set(TableCollection.DESCRIPTION, collection.description)
      .set(TableCollection.LIBRARY_ID, collection.libraryId)
      .execute()
  }

  @Transactional
  override fun update(collection: DomainCollection) {
    dsl.update(TableCollection)
      .set(TableCollection.ID, collection.id)
      .set(TableCollection.NAME, collection.name)
      .set(TableCollection.DESCRIPTION, collection.description)
      .set(TableCollection.LIBRARY_ID, collection.libraryId)
      .set(TableCollection.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableCollection.ID.eq(collection.id))
      .execute()
  }

  @Transactional
  override fun delete(collectionId: String) {
    dsl.deleteFrom(TableCollection)
      .where(TableCollection.ID.eq(collectionId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableCollection).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableCollection).toLong()

  private fun CollectionRecord.toDomain(): DomainCollection = DomainCollection(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}