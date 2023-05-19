package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.model.PersonLinks
import io.github.alessandrojean.tankobon.domain.model.PersonSearch
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneEntity
import io.github.alessandrojean.tankobon.infrastructure.search.LuceneHelper
import io.github.alessandrojean.tankobon.jooq.tables.records.PersonRecord
import org.jooq.Condition
import org.jooq.DSLContext
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
import io.github.alessandrojean.tankobon.jooq.Tables.PERSON as TablePerson

@Component
class PersonDao(
  private val dsl: DSLContext,
  private val libraryDao: LibraryDao,
  private val userDao: TankobonUserDao,
  private val luceneHelper: LuceneHelper,
) : PersonRepository {

  private val sorts = mapOf(
    "name" to TablePerson.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TablePerson.CREATED_AT,
    "modifiedAt" to TablePerson.MODIFIED_AT,
  )

  override fun findById(personId: String): Person = findByIdOrNull(personId)!!

  override fun findByIdOrNull(personId: String): Person? =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.ID.eq(personId))
      .fetchOne()
      ?.toDomain()

  override fun findByNameInLibraryOrNull(name: String, libraryId: String): Person? =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.LIBRARY_ID.eq(libraryId))
      .and(TablePerson.NAME.equalIgnoreCase(name))
      .fetchOne()
      ?.toDomain()

  override fun findByNamesInLibraryOrNull(names: Collection<String>, libraryId: String): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.LIBRARY_ID.eq(libraryId))
      .and(
        names.map { TablePerson.NAME.equalIgnoreCase(it) }
          .fold(noCondition(), Condition::or)
      )
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun findByLibraryId(libraryId: String): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.LIBRARY_ID.eq(libraryId))
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun findAll(): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun findAll(search: PersonSearch, pageable: Pageable): Page<Person> {
    val peopleId = luceneHelper.searchEntitiesIds(search.searchTerm, LuceneEntity.Person)
    val searchCondition = TablePerson.ID.inOrNoCondition(peopleId)

    val conditions = search.toCondition()
      .and(searchCondition)

    val count = dsl.fetchCount(
      dsl.selectDistinct(TablePerson.ID)
        .from(TablePerson)
        .where(conditions)
    )

    val orderBy = pageable.sort.mapNotNull {
      if (it.property == "relevance" && !peopleId.isNullOrEmpty()) {
        TablePerson.ID.sortByValues(peopleId, it.isAscending)
      } else {
        it.toSortField(sorts)
      }
    }

    val people = dsl.selectFrom(TablePerson)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TablePerson)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    val pageRequest = if (pageable.isPaged) {
      PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
    } else {
      PageRequest.of(0, maxOf(count, 20), pageSort)
    }

    return PageImpl(
      people,
      pageRequest,
      count.toLong(),
    )
  }

  override fun findAllByIds(personIds: Collection<String>): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.ID.`in`(personIds))
      .orderBy(TablePerson.ID.sortByValues(personIds.toList(), true))
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TablePerson)
        .where(TablePerson.NAME.equalIgnoreCase(name))
        .and(TablePerson.LIBRARY_ID.eq(libraryId))
    )

  override fun getLibraryIdOrNull(personId: String): String? =
    dsl.select(TablePerson.LIBRARY_ID)
      .from(TablePerson)
      .where(TablePerson.ID.eq(personId))
      .fetchOne(TablePerson.LIBRARY_ID)

  @Transactional
  override fun insert(person: Person) {
    dsl.insertInto(TablePerson)
      .set(TablePerson.ID, person.id)
      .set(TablePerson.NAME, person.name)
      .set(TablePerson.DESCRIPTION, person.description)
      .set(TablePerson.LIBRARY_ID, person.libraryId)
      .set(TablePerson.WEBSITE, person.links.website)
      .set(TablePerson.TWITTER, person.links.twitter)
      .set(TablePerson.INSTAGRAM, person.links.instagram)
      .set(TablePerson.FACEBOOK, person.links.facebook)
      .set(TablePerson.PIXIV, person.links.pixiv)
      .set(TablePerson.DEVIANTART, person.links.deviantArt)
      .set(TablePerson.YOUTUBE, person.links.youTube)
      .execute()
  }

  @Transactional
  override fun update(person: Person) {
    dsl.update(TablePerson)
      .set(TablePerson.ID, person.id)
      .set(TablePerson.NAME, person.name)
      .set(TablePerson.DESCRIPTION, person.description)
      .set(TablePerson.LIBRARY_ID, person.libraryId)
      .set(TablePerson.WEBSITE, person.links.website)
      .set(TablePerson.TWITTER, person.links.twitter)
      .set(TablePerson.INSTAGRAM, person.links.instagram)
      .set(TablePerson.FACEBOOK, person.links.facebook)
      .set(TablePerson.PIXIV, person.links.pixiv)
      .set(TablePerson.DEVIANTART, person.links.deviantArt)
      .set(TablePerson.YOUTUBE, person.links.youTube)
      .set(TablePerson.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TablePerson.ID.eq(person.id))
      .execute()
  }

  @Transactional
  override fun delete(personId: String) {
    dsl.deleteFrom(TablePerson)
      .where(TablePerson.ID.eq(personId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TablePerson).execute()
  }

  override fun count(): Long = dsl.fetchCount(TablePerson).toLong()

  private fun PersonSearch.toCondition(): Condition {
    val c = noCondition()

    if (userId.isNullOrEmpty()) {
      return c
    }

    val user = userDao.findByIdOrNull(userId)!!
    val librariesIdsUserHasAccess = libraryDao.getAllowedToViewLibrariesIds(userId)
    val filteredLibrariesIds = libraryIds.orEmpty()
      .filter { it in librariesIdsUserHasAccess }

    val libraryCondition = when {
      user.isAdmin && !libraryIds.isNullOrEmpty() ->
        TablePerson.LIBRARY_ID.`in`(libraryIds)
      user.isAdmin -> noCondition()
      !libraryIds.isNullOrEmpty() ->
        TablePerson.LIBRARY_ID.inOrNoCondition(filteredLibrariesIds)
      else -> noCondition()
    }

    return c.and(libraryCondition)
  }

  private fun PersonRecord.toDomain(): Person = Person(
    name = name,
    description = description,
    links = PersonLinks(
      website = website,
      twitter = twitter,
      instagram = instagram,
      facebook = facebook,
      pixiv = pixiv,
      deviantArt = deviantart,
      youTube = youtube,
    ),
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}