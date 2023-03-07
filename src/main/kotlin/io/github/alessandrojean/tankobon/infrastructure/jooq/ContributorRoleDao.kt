package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.ContributorRoleRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.CONTRIBUTOR_ROLE as TableContributorRole

@Component
class ContributorRoleDao(
  private val dsl: DSLContext,
) : ContributorRoleRepository {

  override fun findById(contributorRoleId: String): ContributorRole = findByIdOrNull(contributorRoleId)!!

  override fun findByIdOrNull(contributorRoleId: String): ContributorRole? =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.ID.eq(contributorRoleId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun findAll(): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun findAllByIds(contributorRoleIds: Collection<String>): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.ID.`in`(contributorRoleIds))
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableContributorRole)
        .where(TableContributorRole.NAME.equalIgnoreCase(name))
        .and(TableContributorRole.LIBRARY_ID.eq(libraryId))
    )

  override fun getLibraryIdOrNull(contributorRoleId: String): String? =
    dsl.select(TableContributorRole.LIBRARY_ID)
      .from(TableContributorRole)
      .where(TableContributorRole.ID.eq(contributorRoleId))
      .fetchOne(TableContributorRole.LIBRARY_ID)

  @Transactional
  override fun insert(contributorRole: ContributorRole) {
    dsl.insertInto(TableContributorRole)
      .set(TableContributorRole.ID, contributorRole.id)
      .set(TableContributorRole.NAME, contributorRole.name)
      .set(TableContributorRole.DESCRIPTION, contributorRole.description)
      .set(TableContributorRole.LIBRARY_ID, contributorRole.libraryId)
      .execute()
  }

  @Transactional
  override fun update(contributorRole: ContributorRole) {
    dsl.update(TableContributorRole)
      .set(TableContributorRole.ID, contributorRole.id)
      .set(TableContributorRole.NAME, contributorRole.name)
      .set(TableContributorRole.DESCRIPTION, contributorRole.description)
      .set(TableContributorRole.LIBRARY_ID, contributorRole.libraryId)
      .set(TableContributorRole.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableContributorRole.ID.eq(contributorRole.id))
      .execute()
  }

  @Transactional
  override fun delete(contributorRoleId: String) {
    dsl.deleteFrom(TableContributorRole)
      .where(TableContributorRole.ID.eq(contributorRoleId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableContributorRole).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableContributorRole).toLong()

  private fun ContributorRoleRecord.toDomain(): ContributorRole = ContributorRole(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}