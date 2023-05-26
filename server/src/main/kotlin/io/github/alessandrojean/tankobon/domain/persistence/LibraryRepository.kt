package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Library
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LibraryRepository {
  fun findById(libraryId: String): Library
  fun findByIdOrNull(libraryId: String): Library?
  fun findByOwnerId(ownerId: String): Collection<Library>
  fun findByOwnerId(ownerId: String, pageable: Pageable): Page<Library>
  fun findByOwnerIdIncludingShared(ownerId: String): Collection<Library>
  fun findByOwnerIdIncludingShared(ownerId: String, pageable: Pageable): Page<Library>

  fun findAll(): Collection<Library>
  fun findAllByIds(libraryIds: Collection<String>): Collection<Library>

  fun existsByNameFromSameOwner(name: String, ownerId: String): Boolean

  fun getAllowedToViewLibrariesIds(userId: String): Collection<String>

  fun insert(library: Library)
  fun update(library: Library)

  fun delete(libraryId: String)
  fun deleteAll()

  fun count(): Long
}
