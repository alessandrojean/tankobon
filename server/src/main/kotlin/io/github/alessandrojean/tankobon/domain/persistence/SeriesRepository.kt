package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SeriesRepository {
  fun findById(seriesId: String): Series
  fun findByIdOrNull(seriesId: String): Series?
  fun findByIds(seriesIds: Collection<String>): Collection<Series>
  fun findByLibraryId(libraryId: String): Collection<Series>

  fun findAll(): Collection<Series>
  fun findAll(search: SeriesSearch, pageable: Pageable): Page<Series>
  fun findAllByIds(seriesIds: Collection<String>): Collection<Series>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(seriesId: String): String?

  fun insert(series: Series)
  fun update(series: Series)

  fun delete(seriesId: String)
  fun deleteAll()

  fun count(): Long
}
