package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Publisher

interface PublisherRepository {
  fun findById(publisherId: String): Publisher
  fun findByIdOrNull(publisherId: String): Publisher?
  fun findByNameInLibraryOrNull(name: String, libraryId: String): Publisher?
  fun findByLibraryId(libraryId: String): Collection<Publisher>

  fun findAll(): Collection<Publisher>
  fun findAllByIds(publisherIds: Collection<String>): Collection<Publisher>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(publisherId: String): String?

  fun insert(publisher: Publisher)
  fun update(publisher: Publisher)

  fun delete(publisherId: String)
  fun deleteAll()

  fun count(): Long
}
