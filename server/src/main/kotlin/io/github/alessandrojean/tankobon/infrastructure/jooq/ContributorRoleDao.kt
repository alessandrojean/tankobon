package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.ContributorRoleSearch
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.ContributorRoleRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.noCondition
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.CONTRIBUTOR_ROLE as TableContributorRole

@Component
class ContributorRoleDao(
  private val dsl: DSLContext,
  private val userDao: TankobonUserDao,
  private val libraryDao: LibraryDao,
  private val luceneHelper: LuceneHelper,
) : ContributorRoleRepository {

  private val sorts = mapOf(
    "name" to TableContributorRole.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TableContributorRole.CREATED_AT,
    "modifiedAt" to TableContributorRole.MODIFIED_AT,
  )

  override fun findById(contributorRoleId: String): ContributorRole = findByIdOrNull(contributorRoleId)!!

  override fun findByIdOrNull(contributorRoleId: String): ContributorRole? =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.ID.eq(contributorRoleId))
      .fetchOne()
      ?.toDomain()

  override fun findByNameInLibraryOrNull(name: String, libraryId: String): ContributorRole? =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.NAME.equalIgnoreCase(name))
      .and(TableContributorRole.LIBRARY_ID.eq(libraryId))
      .fetchOne()
      ?.toDomain()

  override fun findByNamesInLibraryOrNull(names: Collection<String>, libraryId: String): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.LIBRARY_ID.eq(libraryId))
      .and(
        names.map { TableContributorRole.NAME.equalIgnoreCase(it) }
          .fold(noCondition(), Condition::or)
      )
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun findByLibraryId(libraryId: String): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun findAll(): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

  override fun findAll(search: ContributorRoleSearch, pageable: Pageable): Page<ContributorRole> {
    val contributorRolesIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.ContributorRole)
    val searchCondition = TableContributorRole.ID.inOrNoCondition(contributorRolesIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TableContributorRole.ID)
        .from(TableContributorRole)
        .where(conditions)
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !contributorRolesIds.isNullOrEmpty()) {
        TableContributorRole.ID.sortByValues(contributorRolesIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val contributorRoles = dsl.selectFrom(TableContributorRole)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableContributorRole)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      contributorRoles,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(contributorRoleIds: Collection<String>): Collection<ContributorRole> =
    dsl.selectFrom(TableContributorRole)
      .where(TableContributorRole.ID.`in`(contributorRoleIds))
      .orderBy(TableContributorRole.ID.sortByValues(contributorRoleIds.toList(), true))
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

  private fun ContributorRoleSearch.toCondition(): Condition {
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
        TableContributorRole.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TableContributorRole.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun ContributorRoleRecord.toDomain(): ContributorRole = ContributorRole(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}