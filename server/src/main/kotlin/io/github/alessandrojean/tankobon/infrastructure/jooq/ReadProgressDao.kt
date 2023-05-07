package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import io.github.alessandrojean.tankobon.domain.persistence.ReadProgressRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.ReadProgressRecord
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.BOOK as TableBook
import io.github.alessandrojean.tankobon.jooq.Tables.COLLECTION as TableCollection
import io.github.alessandrojean.tankobon.jooq.Tables.READ_PROGRESS as TableReadProgress

@Component
class ReadProgressDao(
  private val dsl: DSLContext,
) : ReadProgressRepository {

  private val sorts = mapOf(
    "startedAt" to TableReadProgress.STARTED_AT,
    "finishedAt" to TableReadProgress.FINISHED_AT,
  )

  override fun findById(readProgressId: String): ReadProgress = findByIdOrNull(readProgressId)!!

  override fun findByIdOrNull(readProgressId: String): ReadProgress? =
    dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.ID.eq(readProgressId))
      .fetchOne()
      ?.toDomain()

  override fun findByBookId(bookId: String): Collection<ReadProgress> =
    dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.BOOK_ID.eq(bookId))
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  override fun findByBookAndUserId(bookId: String, userId: String): Collection<ReadProgress> =
    dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.BOOK_ID.eq(bookId))
      .and(TableReadProgress.USER_ID.eq(userId))
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  override fun findByLibraryId(libraryId: String): Collection<ReadProgress> =
    dsl.select(*TableReadProgress.fields())
      .from(TableReadProgress)
      .leftJoin(TableBook)
      .on(TableBook.ID.eq(TableReadProgress.BOOK_ID))
      .leftJoin(TableCollection)
      .on(TableCollection.ID.eq(TableBook.COLLECTION_ID))
      .where(TableCollection.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  override fun findByUserId(userId: String): Collection<ReadProgress> =
    dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.USER_ID.eq(userId))
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  override fun findByUserId(userId: String, pageable: Pageable): Page<ReadProgress> {
    val count = dsl.fetchCount(
      dsl.selectDistinct(TableReadProgress.ID)
        .from(TableReadProgress)
        .where(TableReadProgress.USER_ID.eq(userId))
    )

    val orderBy = pageable.sort.mapNotNull { it.toSortField(sorts) }

    val readProgresses = dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.USER_ID.eq(userId))
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()

    return PageImpl(
      readProgresses,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort),
      count.toLong(),
    )
  }

  override fun findAll(): Collection<ReadProgress> =
    dsl.selectFrom(TableReadProgress)
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  override fun findAllByIds(readProgressIds: Collection<String>): Collection<ReadProgress> =
    dsl.selectFrom(TableReadProgress)
      .where(TableReadProgress.ID.`in`(readProgressIds))
      .orderBy(TableReadProgress.ID.sortByValues(readProgressIds.toList(), true))
      .fetchInto(TableReadProgress)
      .map { it.toDomain() }

  @Transactional
  override fun insert(readProgress: ReadProgress) {
    dsl.insertInto(TableReadProgress)
      .set(TableReadProgress.ID, readProgress.id)
      .set(TableReadProgress.BOOK_ID, readProgress.bookId)
      .set(TableReadProgress.USER_ID, readProgress.userId)
      .set(TableReadProgress.PAGE, readProgress.page)
      .set(TableReadProgress.STARTED_AT, readProgress.startedAt)
      .set(TableReadProgress.FINISHED_AT, readProgress.finishedAt)
      .set(TableReadProgress.IS_COMPLETED, readProgress.isCompleted)
      .execute()
  }

  @Transactional
  override fun update(readProgress: ReadProgress) {
    dsl.update(TableReadProgress)
      .set(TableReadProgress.BOOK_ID, readProgress.bookId)
      .set(TableReadProgress.USER_ID, readProgress.userId)
      .set(TableReadProgress.PAGE, readProgress.page)
      .set(TableReadProgress.STARTED_AT, readProgress.startedAt)
      .set(TableReadProgress.FINISHED_AT, readProgress.finishedAt)
      .set(TableReadProgress.IS_COMPLETED, readProgress.isCompleted)
      .set(TableReadProgress.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableReadProgress.ID.eq(readProgress.id))
      .execute()
  }

  @Transactional
  override fun delete(readProgressId: String) {
    dsl.deleteFrom(TableReadProgress)
      .where(TableReadProgress.ID.eq(readProgressId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableReadProgress).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableReadProgress).toLong()

  private fun ReadProgressRecord.toDomain() = ReadProgress(
    page = page,
    startedAt = startedAt?.toCurrentTimeZone(),
    finishedAt = finishedAt?.toCurrentTimeZone(),
    isCompleted = isCompleted,
    id = id,
    bookId = bookId,
    userId = userId,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}