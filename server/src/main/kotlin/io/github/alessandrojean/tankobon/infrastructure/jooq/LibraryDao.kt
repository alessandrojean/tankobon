package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.LIBRARY as TableLibrary
import io.github.alessandrojean.tankobon.jooq.Tables.USER_LIBRARY_SHARING as TableUserLibrarySharing

@Component
class LibraryDao(
  private val dsl: DSLContext,
) : LibraryRepository {

  private fun selectBase() =
    dsl
      .select(*TableLibrary.fields())
      .select(TableUserLibrarySharing.USER_ID)
      .from(TableLibrary)
      .leftJoin(TableUserLibrarySharing)
      .onKey()

  override fun findById(libraryId: String): Library = findOne(libraryId)!!

  override fun findByIdOrNull(libraryId: String): Library? = findOne(libraryId)

  private fun findOne(libraryId: String) =
    selectBase()
      .where(TableLibrary.ID.eq(libraryId))
      .fetchAndMap()
      .firstOrNull()

  override fun findByOwnerId(ownerId: String): Collection<Library> =
    selectBase()
      .where(TableLibrary.OWNER_ID.eq(ownerId))
      .fetchAndMap()

  override fun findByOwnerIdIncludingShared(ownerId: String): Collection<Library> {
    return selectBase()
      .where(TableLibrary.OWNER_ID.eq(ownerId))
      .or(
        TableLibrary.ID.`in`(
          dsl.select(TableUserLibrarySharing.LIBRARY_ID)
            .from(TableUserLibrarySharing)
            .where(TableUserLibrarySharing.USER_ID.eq(ownerId))
        )
      )
      .fetchAndMap()
  }

  override fun findAll(): Collection<Library> = selectBase().fetchAndMap()

  override fun findAllByIds(libraryIds: Collection<String>): Collection<Library> =
    selectBase()
      .where(TableLibrary.ID.`in`(libraryIds))
      .orderBy(TableLibrary.ID.sortByValues(libraryIds.toList(), true))
      .fetchAndMap()

  override fun getAllowedToViewLibrariesIds(userId: String): Collection<String> =
    dsl.select(TableLibrary.ID, TableUserLibrarySharing.USER_ID)
      .from(TableLibrary)
      .leftJoin(TableUserLibrarySharing)
      .on(TableLibrary.ID.eq(TableUserLibrarySharing.LIBRARY_ID))
      .fetch(TableLibrary.ID)

  override fun existsByNameFromSameOwner(name: String, ownerId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableLibrary)
        .where(TableLibrary.NAME.equalIgnoreCase(name))
        .and(TableLibrary.OWNER_ID.equal(ownerId))
    )

  @Transactional
  override fun insert(library: Library) {
    dsl.insertInto(TableLibrary)
      .set(TableLibrary.ID, library.id)
      .set(TableLibrary.NAME, library.name)
      .set(TableLibrary.DESCRIPTION, library.description)
      .set(TableLibrary.OWNER_ID, library.ownerId)
      .execute()

    insertSharedUsers(library)
  }

  @Transactional
  override fun update(library: Library) {
    dsl.update(TableLibrary)
      .set(TableLibrary.NAME, library.name)
      .set(TableLibrary.DESCRIPTION, library.description)
      .set(TableLibrary.OWNER_ID, library.ownerId)
      .set(TableLibrary.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableLibrary.ID.eq(library.id))
      .execute()

    dsl.deleteFrom(TableUserLibrarySharing)
      .where(TableUserLibrarySharing.LIBRARY_ID.eq(library.id))
      .execute()

    insertSharedUsers(library)
  }

  private fun insertSharedUsers(library: Library) {
    library.sharedUsersIds.forEach {
      dsl.insertInto(TableUserLibrarySharing)
        .columns(TableUserLibrarySharing.USER_ID, TableUserLibrarySharing.LIBRARY_ID)
        .values(it, library.id)
        .execute()
    }
  }

  @Transactional
  override fun delete(libraryId: String) {
    dsl.deleteFrom(TableUserLibrarySharing).where(TableUserLibrarySharing.LIBRARY_ID.eq(libraryId)).execute()
    dsl.deleteFrom(TableLibrary).where(TableLibrary.ID.eq(libraryId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableUserLibrarySharing).execute()
    dsl.deleteFrom(TableLibrary).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableLibrary).toLong()

  private fun ResultQuery<Record>.fetchAndMap() =
    this.fetchGroups({ it.into(TableLibrary) }, { it.into(TableUserLibrarySharing) })
      .map { (lr, ulr) ->
        Library(
          name = lr.name,
          description = lr.description,
          id = lr.id,
          ownerId = lr.ownerId,
          sharedUsersIds = ulr.mapNotNull { it.userId }.toSet(),
          createdAt = lr.createdAt.toCurrentTimeZone(),
          modifiedAt = lr.modifiedAt.toCurrentTimeZone()
        )
      }

}