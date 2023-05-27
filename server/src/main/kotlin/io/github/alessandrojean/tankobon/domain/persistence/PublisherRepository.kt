package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.PublisherSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PublisherRepository {
  fun findById(publisherId: String): Publisher
  fun findByIdOrNull(publisherId: String): Publisher?
  fun findByNameInLibraryOrNull(name: String, libraryId: String): Publisher?
  fun findByLibraryId(libraryId: String): Collection<Publisher>

  fun findAll(): Collection<Publisher>
  fun findAll(search: PublisherSearch, pageable: Pageable): Page<Publisher>
  fun findAllByIds(publisherIds: Collection<String>): Collection<Publisher>
  fun findAllByIds(publisherIds: Collection<String>, libraryId: String): Collection<Publisher>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(publisherId: String): String?

  fun insert(publisher: Publisher)
  fun update(publisher: Publisher)

  fun delete(publisherId: String)
  fun deleteAll()

  fun count(): Long
}
