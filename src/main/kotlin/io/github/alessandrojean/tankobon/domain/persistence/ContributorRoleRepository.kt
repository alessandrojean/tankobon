package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.ContributorRole

interface ContributorRoleRepository {
  fun findById(contributorRoleId: String): ContributorRole
  fun findByIdOrNull(contributorRoleId: String): ContributorRole?
  fun findByLibraryId(libraryId: String): Collection<ContributorRole>

  fun findAll(): Collection<ContributorRole>
  fun findAllByIds(contributorRoleIds: Collection<String>): Collection<ContributorRole>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun insert(contributorRole: ContributorRole)
  fun update(contributorRole: ContributorRole)

  fun delete(contributorRoleId: String)
  fun deleteAll()

  fun count(): Long
}
