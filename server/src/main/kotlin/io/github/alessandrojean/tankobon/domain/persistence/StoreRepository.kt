package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.StoreSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StoreRepository {
  fun findById(storeId: String): Store
  fun findByIdOrNull(storeId: String): Store?
  fun findByLibraryId(libraryId: String): Collection<Store>

  fun findAll(): Collection<Store>
  fun findAll(search: StoreSearch, pageable: Pageable): Page<Store>
  fun findAllByIds(storeIds: Collection<String>): Collection<Store>
  fun findAllByIds(storeIds: Collection<String>, libraryId: String): Collection<Store>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(storeId: String): String?

  fun insert(store: Store)
  fun update(store: Store)

  fun delete(storeId: String)
  fun deleteAll()

  fun count(): Long
}
