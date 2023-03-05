package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.PersonRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.PERSON as TablePerson

@Component
class PersonDao(
  private val dsl: DSLContext,
) : PersonRepository {

  override fun findById(personId: String): Person = findByIdOrNull(personId)!!

  override fun findByIdOrNull(personId: String): Person? =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.ID.eq(personId))
      .fetchOne()
      ?.toDomain()

  override fun findByLibraryId(libraryId: String): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.LIBRARY_ID.eq(libraryId))
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun findAll(): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun findAllByIds(personIds: Collection<String>): Collection<Person> =
    dsl.selectFrom(TablePerson)
      .where(TablePerson.ID.`in`(personIds))
      .fetchInto(TablePerson)
      .map { it.toDomain() }

  override fun existsByNameInLibrary(name: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TablePerson)
        .where(TablePerson.NAME.equalIgnoreCase(name))
        .and(TablePerson.LIBRARY_ID.eq(libraryId))
    )

  @Transactional
  override fun insert(person: Person) {
    dsl.insertInto(TablePerson)
      .set(TablePerson.ID, person.id)
      .set(TablePerson.NAME, person.name)
      .set(TablePerson.DESCRIPTION, person.description)
      .set(TablePerson.LIBRARY_ID, person.libraryId)
      .execute()
  }

  @Transactional
  override fun update(person: Person) {
    dsl.update(TablePerson)
      .set(TablePerson.ID, person.id)
      .set(TablePerson.NAME, person.name)
      .set(TablePerson.DESCRIPTION, person.description)
      .set(TablePerson.LIBRARY_ID, person.libraryId)
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

  private fun PersonRecord.toDomain(): Person = Person(
    name = name,
    description = description,
    libraryId = libraryId,
    id = id,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}