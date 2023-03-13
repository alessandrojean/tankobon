package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.TagRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.TAG as TableTag

@Component
class TagDao(
  private val dsl: DSLContext,
) : TagRepository {

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

  private fun TagRecord.toDomain(): Tag = Tag(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}