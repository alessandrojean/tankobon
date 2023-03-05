package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.StoreRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.STORE as TableStore

@Component
class StoreDao(
  private val dsl: DSLContext,
) : StoreRepository {

  override fun findById(storeId: String): Store = findByIdOrNull(storeId)!!

  override fun findByIdOrNull(storeId: String): Store? =
    dsl.selectFrom(TableStore)
      .where(TableStore.ID.eq(storeId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Store> =
    dsl.selectFrom(TableStore)
      .where(TableStore.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun findAll(): Collection<Store> =
    dsl.selectFrom(TableStore)
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun findAllByIds(storeIds: Collection<String>): Collection<Store> =
    dsl.selectFrom(TableStore)
      .where(TableStore.ID.`in`(storeIds))
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableStore)
        .where(TableStore.NAME.equalIgnoreCase(name))
        .and(TableStore.LIBRARY_ID.eq(libraryId))
    )

  @Transactional
  override fun insert(store: Store) {
    dsl.insertInto(TableStore)
      .set(TableStore.ID, store.id)
      .set(TableStore.NAME, store.name)
      .set(TableStore.DESCRIPTION, store.description)
      .set(TableStore.LIBRARY_ID, store.libraryId)
      .execute()
  }

  @Transactional
  override fun update(store: Store) {
    dsl.update(TableStore)
      .set(TableStore.ID, store.id)
      .set(TableStore.NAME, store.name)
      .set(TableStore.DESCRIPTION, store.description)
      .set(TableStore.LIBRARY_ID, store.libraryId)
      .set(TableStore.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableStore.ID.eq(store.id))
      .execute()
  }

  @Transactional
  override fun delete(storeId: String) {
    dsl.deleteFrom(TableStore)
      .where(TableStore.ID.eq(storeId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableStore).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableStore).toLong()

  private fun StoreRecord.toDomain(): Store = Store(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}