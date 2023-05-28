package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.CollectionSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import io.github.alessandrojean.tankobon.domain.model.Collection as DomainCollection

interface CollectionRepository {
  fun findById(collectionId: String): DomainCollection
  fun findByIdOrNull(collectionId: String): DomainCollection?
  fun findByLibraryId(libraryId: String): Collection<DomainCollection>

  fun findAll(): Collection<DomainCollection>
  fun findAll(search: CollectionSearch, pageable: Pageable): Page<DomainCollection>
  fun findAllByIds(collectionIds: Collection<String>): Collection<DomainCollection>
  fun findAllByIds(collectionIds: Collection<String>, libraryId: String): Collection<DomainCollection>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(collectionId: String): String?

  fun insert(collection: DomainCollection)
  fun update(collection: DomainCollection)

  fun delete(collectionId: String)
  fun deleteAll()

  fun count(): Long
}
