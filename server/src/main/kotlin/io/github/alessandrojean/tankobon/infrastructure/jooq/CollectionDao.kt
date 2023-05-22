package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.CollectionSearch
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.CollectionRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
  private val libraryDao: LibraryDao,
  private val userDao: TankobonUserDao,
  private val luceneHelper: LuceneHelper,
) : CollectionRepository {

  private val sorts = mapOf(
    "name" to TableCollection.NAME.collate(SqliteUdfDataSource.collationUnicode3),
  )

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

  override fun findAll(search: CollectionSearch, pageable: Pageable): Page<DomainCollection> {
    val collectionIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Collection)
    val searchCondition = TableCollection.ID.inOrNoCondition(collectionIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TableCollection.ID)
        .from(TableCollection)
        .where(conditions),
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !collectionIds.isNullOrEmpty()) {
        TableCollection.ID.sortByValues(collectionIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val collections = dsl.selectFrom(TableCollection)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableCollection)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      collections,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(collectionIds: Collection<String>): Collection<DomainCollection> =
    dsl.selectFrom(TableCollection)
      .where(TableCollection.ID.`in`(collectionIds))
      .orderBy(TableCollection.ID.sortByValues(collectionIds.toList(), true))
      .fetchInto(TableCollection)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableCollection)
        .where(TableCollection.NAME.equalIgnoreCase(name))
        .and(TableCollection.LIBRARY_ID.eq(libraryId)),
    )

  override fun getLibraryIdOrNull(collectionId: String): String? =
    dsl.select(TableCollection.LIBRARY_ID)
      .from(TableCollection)
      .where(TableCollection.ID.eq(collectionId))
      .fetchOne(TableCollection.LIBRARY_ID)

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

  private fun CollectionSearch.toCondition(): Condition {
    val c = DSL.noCondition()

    if (userId.isNullOrEmpty()) {
      return c
    }

    val user = userDao.findByIdOrNull(userId)!!
    val librariesIdsUserHasAccess = libraryDao.getAllowedToViewLibrariesIds(userId)
    val filteredLibrariesIds = libraryIds.orEmpty()
      .filter { it in librariesIdsUserHasAccess }

    val libraryCondition = when {
      user.isAdmin && !libraryIds.isNullOrEmpty() ->
        TableCollection.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TableCollection.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun CollectionRecord.toDomain(): DomainCollection = DomainCollection(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )
}
