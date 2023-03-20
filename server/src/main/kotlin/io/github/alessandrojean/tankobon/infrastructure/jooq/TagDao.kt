package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TagSearch
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.TagRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.TAG as TableTag

@Component
class TagDao(
  private val dsl: DSLContext,
  private val userDao: TankobonUserDao,
  private val libraryDao: LibraryDao,
  private val luceneHelper: LuceneHelper,
) : TagRepository {

  private val sorts = mapOf(
    "name" to TableTag.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TableTag.CREATED_AT,
    "modifiedAt" to TableTag.MODIFIED_AT,
  )

  override fun findById(tagId: String): Tag = findByIdOrNull(tagId)!!

  override fun findByIdOrNull(tagId: String): Tag? =
    dsl.selectFrom(TableTag)
      .where(TableTag.ID.eq(tagId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Tag> =
    dsl.selectFrom(TableTag)
      .where(TableTag.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableTag)
      .map { it.toDomain() }

  override fun findAll(): Collection<Tag> =
    dsl.selectFrom(TableTag)
      .fetchInto(TableTag)
      .map { it.toDomain() }

  override fun findAll(search: TagSearch, pageable: Pageable): Page<Tag> {
    val tagsIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Tag)
    val searchCondition = TableTag.ID.inOrNoCondition(tagsIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TableTag.ID)
        .from(TableTag)
        .where(conditions)
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !tagsIds.isNullOrEmpty()) {
        TableTag.ID.sortByValues(tagsIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val tags = dsl.selectFrom(TableTag)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableTag)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()

    return PageImpl(
      tags,
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort),
      count.toLong(),
    )
  }

  override fun findAllByIds(tagIds: Collection<String>): Collection<Tag> =
    dsl.selectFrom(TableTag)
      .where(TableTag.ID.`in`(tagIds))
      .fetchInto(TableTag)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableTag)
        .where(TableTag.NAME.equalIgnoreCase(name))
        .and(TableTag.LIBRARY_ID.eq(libraryId))
    )

  override fun getLibraryIdOrNull(tagId: String): String? =
    dsl.select(TableTag.LIBRARY_ID)
      .from(TableTag)
      .where(TableTag.ID.eq(tagId))
      .fetchOne(TableTag.LIBRARY_ID)

  @Transactional
  override fun insert(tag: Tag) {
    dsl.insertInto(TableTag)
      .set(TableTag.ID, tag.id)
      .set(TableTag.NAME, tag.name)
      .set(TableTag.DESCRIPTION, tag.description)
      .set(TableTag.LIBRARY_ID, tag.libraryId)
      .execute()
  }

  @Transactional
  override fun update(tag: Tag) {
    dsl.update(TableTag)
      .set(TableTag.ID, tag.id)
      .set(TableTag.NAME, tag.name)
      .set(TableTag.DESCRIPTION, tag.description)
      .set(TableTag.LIBRARY_ID, tag.libraryId)
      .set(TableTag.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableTag.ID.eq(tag.id))
      .execute()
  }

  @Transactional
  override fun delete(tagId: String) {
    dsl.deleteFrom(TableTag)
      .where(TableTag.ID.eq(tagId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableTag).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableTag).toLong()

  private fun TagSearch.toCondition(): Condition {
    val c = DSL.noCondition()

    if (userId.isNullOrEmpty()) {
      return c
    }

    val user = userDao.findByIdOrNull(userId)!!
    val librariesIdsUserHasAccess = libraryDao.getAllowedToViewLibrariesIds(userId)
    val filteredLibrariesIds = libraryIds.orEmpty()
      .filter { it in librariesIdsUserHasAccess }

    val libraryCondition = when {
      user.isAdmin && !libraryIds.isNullOrEmpty() ->
        TableTag.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TableTag.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun TagRecord.toDomain(): Tag = Tag(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}