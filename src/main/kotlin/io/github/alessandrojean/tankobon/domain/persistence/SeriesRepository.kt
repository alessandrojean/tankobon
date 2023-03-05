package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Series

interface SeriesRepository {
  fun findById(seriesId: String): Series
  fun findByIdOrNull(seriesId: String): Series?
  fun findByLibraryId(libraryId: String): Collection<Series>

  fun findAll(): Collection<Series>
  fun findAllByIds(seriesIds: Collection<String>): Collection<Series>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun insert(series: Series)
  fun update(series: Series)

  fun delete(seriesId: String)
  fun deleteAll()

  fun count(): Long
}
