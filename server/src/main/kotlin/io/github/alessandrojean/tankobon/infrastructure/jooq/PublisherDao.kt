package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.PublisherSearch
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.PublisherRecord
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
import io.github.alessandrojean.tankobon.jooq.Tables.PUBLISHER as TablePublisher

@Component
class PublisherDao(
  private val dsl: DSLContext,
  private val userDao: TankobonUserDao,
  private val libraryDao: LibraryDao,
  private val luceneHelper: LuceneHelper,
) : PublisherRepository {

  private val sorts = mapOf(
    "name" to TablePublisher.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TablePublisher.CREATED_AT,
    "modifiedAt" to TablePublisher.MODIFIED_AT,
  )

  override fun findById(publisherId: String): Publisher = findByIdOrNull(publisherId)!!

  override fun findByIdOrNull(publisherId: String): Publisher? =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.ID.eq(publisherId))
      .fetchOne()
      ?.toDomain()

  override fun findByNameInLibraryOrNull(name: String, libraryId: String): Publisher? =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.LIBRARY_ID.eq(libraryId))
      .and(TablePublisher.NAME.equalIgnoreCase(name))
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

  override fun findAll(search: PublisherSearch, pageable: Pageable): Page<Publisher> {
    val publishersIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Publisher)
    val searchCondition = TablePublisher.ID.inOrNoCondition(publishersIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TablePublisher.ID)
        .from(TablePublisher)
        .where(conditions)
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !publishersIds.isNullOrEmpty()) {
        TablePublisher.ID.sortByValues(publishersIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val publishers = dsl.selectFrom(TablePublisher)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TablePublisher)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      publishers,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(publisherIds: Collection<String>): Collection<Publisher> =
    dsl.selectFrom(TablePublisher)
      .where(TablePublisher.ID.`in`(publisherIds))
      .orderBy(TablePublisher.ID.sortByValues(publisherIds.toList(), true))
      .fetchInto(TablePublisher)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TablePublisher)
        .where(TablePublisher.NAME.equalIgnoreCase(name))
        .and(TablePublisher.LIBRARY_ID.eq(libraryId))
    )

  override fun getLibraryIdOrNull(publisherId: String): String? =
    dsl.select(TablePublisher.LIBRARY_ID)
      .from(TablePublisher)
      .where(TablePublisher.ID.eq(publisherId))
      .fetchOne(TablePublisher.LIBRARY_ID)

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

  private fun PublisherSearch.toCondition(): Condition {
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
        TablePublisher.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TablePublisher.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun PublisherRecord.toDomain(): Publisher = Publisher(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}