package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Book

interface BookRepository {
  fun findByIdOrNull(bookId: String): Book?
  fun findByCodeOrNull(bookCode: String): Book?

  fun findAll(): Collection<Book>
  fun findAllByIds(bookIds: Collection<String>): Collection<Book>
  fun findAllBySeriesId(seriesId: String): Collection<Book>
  fun findAllByStoreId(storeId: String): Collection<Book>
  fun findAllByLibraryId(libraryId: String): Collection<Book>

  fun findFirstIdInSeriesOrNull(seriesId: String): String?
  fun findLastIdInSeriesOrNull(seriesId: String): String?

  fun findAllIdsBySeriesId(seriesId: String): Collection<String>
  fun findAllIdsByLibraryId(libraryId: String): Collection<String>

  fun existsByCode(code: String): Boolean

  fun getLibraryIdOrNull(bookId: String): String?

  fun insert(book: Book)
  fun update(book: Book)

  fun delete(bookId: String)
  fun deleteAll()

  fun count(): Long
}