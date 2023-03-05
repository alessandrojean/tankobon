package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.SeriesRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.SERIES as TableSeries

@Component
class SeriesDao(
  private val dsl: DSLContext,
) : SeriesRepository {

  override fun findById(seriesId: String): Series = findByIdOrNull(seriesId)!!

  override fun findByIdOrNull(seriesId: String): Series? =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.ID.eq(seriesId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .where(TableSeries.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableSeries)
      .map { it.toDomain() }

  override fun findAll(): Collection<Series> =
    dsl.selectFrom(TableSeries)
      .fetchInto(TableSeries)
      .map { it.toDomain() }

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

  private fun SeriesRecord.toDomain(): Series = Series(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}