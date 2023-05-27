package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.StoreLinks
import io.github.alessandrojean.tankobon.domain.model.StoreSearch
import io.github.alessandrojean.tankobon.domain.model.StoreType
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.StoreRecord
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
import io.github.alessandrojean.tankobon.jooq.Tables.STORE as TableStore

@Component
class StoreDao(
  private val dsl: DSLContext,
  private val userDao: TankobonUserDao,
  private val libraryDao: LibraryDao,
  private val luceneHelper: LuceneHelper,
) : StoreRepository {

  private val sorts = mapOf(
    "legalName" to TableStore.LEGAL_NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "location" to TableStore.LOCATION,
    "name" to TableStore.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TableStore.CREATED_AT,
    "modifiedAt" to TableStore.MODIFIED_AT,
  )

  override fun findById(storeId: String): Store = findByIdOrNull(storeId)!!

  override fun findByIdOrNull(storeId: String): Store? =
    dsl.selectFrom(TableStore)
      .where(TableStore.ID.eq(storeId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Store> =
    dsl.selectFrom(TableStore)
      .where(TableStore.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun findAll(): Collection<Store> =
    dsl.selectFrom(TableStore)
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun findAll(search: StoreSearch, pageable: Pageable): Page<Store> {
    val storesIds = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Store)
    val searchCondition = TableStore.ID.inOrNoCondition(storesIds)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TableStore)
        .from(TableStore)
        .where(conditions),
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !storesIds.isNullOrEmpty()) {
        TableStore.ID.sortByValues(storesIds, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val stores = dsl.selectFrom(TableStore)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableStore)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      stores,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(storeIds: Collection<String>): Collection<Store> =
    dsl.selectFrom(TableStore)
      .where(TableStore.ID.`in`(storeIds))
      .orderBy(TableStore.ID.sortByValues(storeIds.toList(), true))
      .fetchInto(TableStore)
      .map { it.toDomain() }

  override fun findAllByIds(storeIds: Collection<String>, libraryId: String): Collection<Store> {
    if (storeIds.isEmpty()) {
      return emptyList()
    }

    return dsl.selectFrom(TableStore)
      .where(TableStore.ID.`in`(storeIds))
      .and(TableStore.LIBRARY_ID.eq(libraryId))
      .orderBy(TableStore.ID.sortByValues(storeIds.toList(), true))
      .fetchInto(TableStore)
      .map { it.toDomain() }
  }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableStore)
        .where(TableStore.NAME.equalIgnoreCase(name))
        .and(TableStore.LIBRARY_ID.eq(libraryId)),
    )

  override fun getLibraryIdOrNull(storeId: String): String? =
    dsl.select(TableStore.LIBRARY_ID)
      .from(TableStore)
      .where(TableStore.ID.eq(storeId))
      .fetchOne(TableStore.LIBRARY_ID)

  @Transactional
  override fun insert(store: Store) {
    dsl.insertInto(TableStore)
      .set(TableStore.ID, store.id)
      .set(TableStore.NAME, store.name)
      .set(TableStore.DESCRIPTION, store.description)
      .set(TableStore.WEBSITE, store.links.website)
      .set(TableStore.TWITTER, store.links.twitter)
      .set(TableStore.INSTAGRAM, store.links.instagram)
      .set(TableStore.FACEBOOK, store.links.facebook)
      .set(TableStore.YOUTUBE, store.links.youTube)
      .set(TableStore.LEGAL_NAME, store.legalName)
      .set(TableStore.LOCATION, store.location)
      .set(TableStore.TYPE, store.type?.ordinal)
      .set(TableStore.LIBRARY_ID, store.libraryId)
      .execute()
  }

  @Transactional
  override fun update(store: Store) {
    dsl.update(TableStore)
      .set(TableStore.ID, store.id)
      .set(TableStore.NAME, store.name)
      .set(TableStore.DESCRIPTION, store.description)
      .set(TableStore.WEBSITE, store.links.website)
      .set(TableStore.TWITTER, store.links.twitter)
      .set(TableStore.INSTAGRAM, store.links.instagram)
      .set(TableStore.FACEBOOK, store.links.facebook)
      .set(TableStore.YOUTUBE, store.links.youTube)
      .set(TableStore.LEGAL_NAME, store.legalName)
      .set(TableStore.LOCATION, store.location)
      .set(TableStore.TYPE, store.type?.ordinal)
      .set(TableStore.LIBRARY_ID, store.libraryId)
      .set(TableStore.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableStore.ID.eq(store.id))
      .execute()
  }

  @Transactional
  override fun delete(storeId: String) {
    dsl.deleteFrom(TableStore)
      .where(TableStore.ID.eq(storeId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableStore).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableStore).toLong()

  private fun StoreSearch.toCondition(): Condition {
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
        TableStore.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> DSL.noCondition()
      !libraryIds.isNullOrEmpty() ->
        TableStore.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> DSL.noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun StoreRecord.toDomain(): Store = Store(
    name = name,
    description = description,
    links = StoreLinks(
      website = website,
      twitter = twitter,
      instagram = instagram,
      facebook = facebook,
      youTube = youtube,
    ),
    legalName = legalName,
    location = location,
    type = StoreType.values().getOrNull(type ?: -1),
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )
}
