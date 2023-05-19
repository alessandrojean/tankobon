package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesAlternativeName
import io.github.alessandrojean.tankobon.domain.model.SeriesLinks
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import io.github.alessandrojean.tankobon.domain.model.SeriesType
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.Tables
import io.github.alessandrojean.tankobon.jooq.tables.records.SeriesAlternativeNameRecord
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
import io.github.alessandrojean.tankobon.jooq.Tables.SERIES_ALTERNATIVE_NAME as TableSeriesAlternativeName

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

  override fun findAlternativeNamesById(seriesId: String): List<SeriesAlternativeName> =
    dsl.selectFrom(TableSeriesAlternativeName)
      .where(TableSeriesAlternativeName.SERIES_ID.eq(seriesId))
      .orderBy(TableSeriesAlternativeName.CREATED_AT.asc())
      .fetchInto(TableSeriesAlternativeName)
      .map { it.toDomain() }

  override fun findAlternativeNamesByIds(seriesIds: Collection<String>): Map<String, List<SeriesAlternativeName>> =
    dsl.selectFrom(TableSeriesAlternativeName)
      .where(TableSeriesAlternativeName.SERIES_ID.`in`(seriesIds))
      .orderBy(TableSeriesAlternativeName.CREATED_AT.asc())
      .groupBy({ it.get(TableSeriesAlternativeName.SERIES_ID) }) { it.toDomain() }

  override fun findById(seriesId: String): Series = findByIdOrNull(seriesId)!!

  override fun findByIdOrNull(seriesId: String): Series? {
    val series = dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.eq(seriesId))
      .fetchOne()

    return series?.toDomain(findAlternativeNamesById(seriesId))
  }

  override fun findByIds(seriesIds: Collection<String>): Collection<Series> {
    val alternativeNames = findAlternativeNamesByIds(seriesIds)

    return dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.`in`(seriesIds))
      .fetchInto(TableSeries)
      .map { it.toDomain(alternativeNames[it.id].orEmpty()) }
  }

  override fun findByLibraryId(libraryId: String): Collection<Series> {
    val records = dsl.selectFrom(TableSeries)
      .where(TableSeries.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableSeries)

    val alternativeNames = findAlternativeNamesByIds(records.map { it.id })

    return records.map { it.toDomain(alternativeNames[it.id].orEmpty()) }
  }

  override fun findAll(): Collection<Series> {
    val alternativeNames = dsl.selectFrom(TableSeriesAlternativeName)
      .groupBy({ it.get(TableSeriesAlternativeName.SERIES_ID) }) { it.toDomain() }

    return dsl.selectFrom(TableSeries)
      .fetchInto(TableSeries)
      .map { it.toDomain(alternativeNames[it.id].orEmpty()) }
  }

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

    val seriesRecords = dsl.selectFrom(TableSeries)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableSeries)

    val alternativeNames = findAlternativeNamesByIds(seriesRecords.map { it.id })

    val series = seriesRecords.map { it.toDomain(alternativeNames[it.id].orEmpty()) }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      series,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(seriesIds: Collection<String>): Collection<Series> {
    val alternativeNames = findAlternativeNamesByIds(seriesIds)

    return dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.`in`(seriesIds))
      .orderBy(TableSeries.ID.sortByValues(seriesIds.toList(), true))
      .fetchInto(TableSeries)
      .map { it.toDomain(alternativeNames[it.id].orEmpty()) }
  }

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
      .set(TableSeries.TYPE, series.type?.ordinal)
      .set(TableSeries.LAST_NUMBER, series.lastNumber)
      .set(TableSeries.ORIGINAL_LANGUAGE, series.originalLanguage)
      .set(TableSeries.WEBSITE, series.links.website)
      .set(TableSeries.MY_ANIME_LIST, series.links.myAnimeList)
      .set(TableSeries.KITSU, series.links.kitsu)
      .set(TableSeries.ANILIST, series.links.aniList)
      .set(TableSeries.TWITTER, series.links.twitter)
      .set(TableSeries.INSTAGRAM, series.links.instagram)
      .set(TableSeries.LIBRARY_ID, series.libraryId)
      .execute()

    insertAlternativeNames(series)
  }

  @Transactional
  override fun update(series: Series) {
    dsl.update(TableSeries)
      .set(TableSeries.NAME, series.name)
      .set(TableSeries.DESCRIPTION, series.description)
      .set(TableSeries.TYPE, series.type?.ordinal)
      .set(TableSeries.LAST_NUMBER, series.lastNumber)
      .set(TableSeries.ORIGINAL_LANGUAGE, series.originalLanguage)
      .set(TableSeries.WEBSITE, series.links.website)
      .set(TableSeries.MY_ANIME_LIST, series.links.myAnimeList)
      .set(TableSeries.KITSU, series.links.kitsu)
      .set(TableSeries.ANILIST, series.links.aniList)
      .set(TableSeries.TWITTER, series.links.twitter)
      .set(TableSeries.INSTAGRAM, series.links.instagram)
      .set(TableSeries.LIBRARY_ID, series.libraryId)
      .set(TableSeries.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableSeries.ID.eq(series.id))
      .execute()

    insertAlternativeNames(series)
  }

  private fun insertAlternativeNames(series: Series) {
    dsl.deleteFrom(Tables.SERIES_ALTERNATIVE_NAME)
      .where(Tables.SERIES_ALTERNATIVE_NAME.SERIES_ID.eq(series.id))
      .execute()

    dsl.insertInto(Tables.SERIES_ALTERNATIVE_NAME)
      .columns(
        Tables.SERIES_ALTERNATIVE_NAME.ID,
        Tables.SERIES_ALTERNATIVE_NAME.SERIES_ID,
        Tables.SERIES_ALTERNATIVE_NAME.NAME,
        Tables.SERIES_ALTERNATIVE_NAME.LANGUAGE,
        Tables.SERIES_ALTERNATIVE_NAME.CREATED_AT,
      )
      .apply {
        series.alternativeNames.forEach { alternativeName ->
          values(
            alternativeName.id,
            series.id,
            alternativeName.name,
            alternativeName.language,
            LocalDateTime.now(ZoneId.of("Z"))
          )
        }
      }
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

  private fun SeriesRecord.toDomain(alternativeNames: List<SeriesAlternativeName>): Series = Series(
    name = name,
    description = description,
    type = SeriesType.values().getOrNull(type ?: -1),
    alternativeNames = alternativeNames,
    lastNumber = lastNumber,
    originalLanguage = originalLanguage,
    links = SeriesLinks(
      website = website,
      myAnimeList = myAnimeList,
      kitsu = kitsu,
      aniList = anilist,
      twitter = twitter,
      instagram = instagram,
    ),
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

  private fun SeriesAlternativeNameRecord.toDomain(): SeriesAlternativeName = SeriesAlternativeName(
    name = name,
    language = language,
    id = id,
    seriesId = seriesId,
    createdAt = createdAt,
  )

}