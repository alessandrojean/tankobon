package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.SeriesRecord
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
import io.github.alessandrojean.tankobon.jooq.Tables.SERIES as TableSeries

@Component
class SeriesDao(
  private val dsl: DSLContext,
  private val userDao: TankobonUserDao,
  private val libraryDao: LibraryDao,
  private val luceneHelper: LuceneHelper,
) : SeriesRepository {

  private val sorts = mapOf(
    "createdAt" to TableSeries.CREATED_AT,
    "modifiedAt" to TableSeries.MODIFIED_AT,
    "name" to TableSeries.NAME.collate(SqliteUdfDataSource.collationUnicode3),
  )

  override fun findById(seriesId: String): Series = findByIdOrNull(seriesId)!!

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.eq(seriesId))
      .fetchOne()
      ?.toDomain()

  override fun findByIds(seriesIds: Collection<String>): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.`in`(seriesIds))
      .fetchInto(TableSeries)
      .map { it.toDomain() }

  override fun findByLibraryId(libraryId: String): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableSeries)
      .map { it.toDomain() }

  override fun findAll(): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .fetchInto(TableSeries)
      .map { it.toDomain() }

  override fun findAll(search: SeriesSearch, pageable: Pageable): Page<Series> {
    val seriesIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Series)
    val searchCondition = TableSeries.ID.inOrNoCondition(seriesIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TableSeries.ID)
        .from(TableSeries)
        .where(conditions)
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !seriesIds.isNullOrEmpty()) {
        TableSeries.ID.sortByValues(seriesIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val series = dsl.selectFrom(TableSeries)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableSeries)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()

    return PageImpl(
      series,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort),
      count.toLong(),
    )
  }

  override fun findAllByIds(seriesIds: Collection<String>): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.`in`(seriesIds))
      .fetchInto(TableSeries)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableSeries)
        .where(TableSeries.NAME.equalIgnoreCase(name))
        .and(TableSeries.LIBRARY_ID.eq(libraryId))
    )

  override fun getLibraryIdOrNull(seriesId: String): String? =
    dsl.select(TableSeries.LIBRARY_ID)
      .from(TableSeries)
      .where(TableSeries.ID.eq(seriesId))
      .fetchOne(TableSeries.LIBRARY_ID)

  @Transactional
  override fun insert(series: Series) {
    dsl.insertInto(TableSeries)
      .set(TableSeries.ID, series.id)
      .set(TableSeries.NAME, series.name)
      .set(TableSeries.DESCRIPTION, series.description)
      .set(TableSeries.LIBRARY_ID, series.libraryId)
      .execute()
  }

  @Transactional
  override fun update(series: Series) {
    dsl.update(TableSeries)
      .set(TableSeries.ID, series.id)
      .set(TableSeries.NAME, series.name)
      .set(TableSeries.DESCRIPTION, series.description)
      .set(TableSeries.LIBRARY_ID, series.libraryId)
      .set(TableSeries.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableSeries.ID.eq(series.id))
      .execute()
  }

  @Transactional
  override fun delete(seriesId: String) {
    dsl.deleteFrom(TableSeries)
      .where(TableSeries.ID.eq(seriesId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableSeries).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableSeries).toLong()

  private fun SeriesSearch.toCondition(): Condition {
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
        TableSeries.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TableSeries.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun SeriesRecord.toDomain(): Series = Series(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}