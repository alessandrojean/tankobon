package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Series

interface SeriesRepository {
  fun findByIdOrNull(seriesId: String): Series?

  fun findAll(): Collection<Series>
  fun findAllByLibraryId(libraryId: String): Collection<Series>
  fun findAllByTitle(title: String): Collection<Series>

  fun getLibraryId(seriesId: String): String?

  fun findAllIdsByLibraryId(libraryId: String): Collection<String>

  fun insert(series: Series)
  fun update(series: Series)

  fun delete(seriesId: String)
  fun delete(seriesIds: Collection<String>)
  fun deleteAll()

  fun count(): Long
  fun countGroupedByLibraryName(): Map<String, Int>
}
