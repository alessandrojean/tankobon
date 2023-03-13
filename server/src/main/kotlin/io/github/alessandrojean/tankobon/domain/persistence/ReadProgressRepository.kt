package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReadProgressRepository {
  fun findById(readProgressId: String): ReadProgress
  fun findByIdOrNull(readProgressId: String): ReadProgress?
  fun findByBookId(bookId: String): Collection<ReadProgress>
  fun findByBookAndUserId(bookId: String, userId: String): Collection<ReadProgress>
  fun findByLibraryId(libraryId: String): Collection<ReadProgress>
  fun findByUserId(userId: String): Collection<ReadProgress>
  fun findByUserId(userId: String, pageable: Pageable): Page<ReadProgress>

  fun findAll(): Collection<ReadProgress>
  fun findAllByIds(readProgressIds: Collection<String>): Collection<ReadProgress>

  fun insert(readProgress: ReadProgress)
  fun update(readProgress: ReadProgress)

  fun delete(readProgressId: String)
  fun deleteAll()

  fun count(): Long
}
