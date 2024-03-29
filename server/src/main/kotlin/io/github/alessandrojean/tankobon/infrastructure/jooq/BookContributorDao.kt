package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.BookContributor
import io.github.alessandrojean.tankobon.domain.persistence.BookContributorRepository
import io.github.alessandrojean.tankobon.infrastructure.image.PersonPictureLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookContributorEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.ReferenceExpansionBookContributor
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.jooq.tables.records.BookContributorRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.BOOK_CONTRIBUTOR as TableBookContributor
import io.github.alessandrojean.tankobon.jooq.Tables.CONTRIBUTOR_ROLE as TableContributorRole
import io.github.alessandrojean.tankobon.jooq.Tables.PERSON as TablePerson

@Component
class BookContributorDao(
  private val dsl: DSLContext,
  private val personPictureLifecycle: PersonPictureLifecycle,
) : BookContributorRepository {

  override fun findByIdOrNull(bookContributorId: String): BookContributor? =
    dsl.selectFrom(TableBookContributor)
      .where(TableBookContributor.ID.eq(bookContributorId))
      .fetchOne()
      ?.toDomain()

  override fun findByIdAsDtoOrNull(bookContributorId: String): BookContributorEntityDto? =
    dsl.select(*TableBookContributor.fields(), *TableContributorRole.fields(), *TablePerson.fields())
      .from(TableBookContributor)
      .leftJoin(TableContributorRole)
      .on(TableContributorRole.ID.eq(TableBookContributor.ROLE_ID))
      .leftJoin(TablePerson)
      .on(TablePerson.ID.eq(TableBookContributor.PERSON_ID))
      .where(TableBookContributor.ID.eq(bookContributorId))
      .fetchOne { record ->
        val domain = record.into(TableBookContributor).toDomain()

        domain.toDto().copy(
          attributes = domain.toAttributesDto(
            roleId = record[TableContributorRole.ID],
            roleName = record[TableContributorRole.NAME],
            personId = record[TablePerson.ID],
            personName = record[TablePerson.NAME],
          ),
        ).withPictureIfExists()
      }

  override fun findAllByIds(bookContributorIds: Collection<String>): Collection<BookContributor> =
    dsl.selectFrom(TableBookContributor)
      .where(TableBookContributor.ID.`in`(bookContributorIds))
      .orderBy(TableBookContributor.ID.sortByValues(bookContributorIds.toList(), true))
      .fetchInto(TableBookContributor)
      .map { it.toDomain() }

  override fun findAllByIdsAsDto(bookContributorIds: Collection<String>): Collection<BookContributorEntityDto> =
    dsl.select(*TableBookContributor.fields(), *TableContributorRole.fields(), *TablePerson.fields())
      .from(TableBookContributor)
      .leftJoin(TableContributorRole)
      .on(TableContributorRole.ID.eq(TableBookContributor.ROLE_ID))
      .leftJoin(TablePerson)
      .on(TablePerson.ID.eq(TableBookContributor.PERSON_ID))
      .where(TableBookContributor.ID.`in`(bookContributorIds))
      .orderBy(TableBookContributor.ID.sortByValues(bookContributorIds.toList(), true))
      .fetch { record ->
        val domain = record.into(TableBookContributor).toDomain()

        domain.toDto().copy(
          attributes = domain.toAttributesDto(
            roleId = record[TableContributorRole.ID],
            roleName = record[TableContributorRole.NAME],
            personId = record[TablePerson.ID],
            personName = record[TablePerson.NAME],
          ),
        )
      }
      .withPictureIfExists()

  override fun findAllByBookId(bookId: String): Collection<BookContributor> =
    dsl.selectFrom(TableBookContributor)
      .where(TableBookContributor.BOOK_ID.eq(bookId))
      .orderBy(TableBookContributor.CREATED_AT.asc())
      .fetchInto(TableBookContributor)
      .map { it.toDomain() }

  override fun findAllAsDtoByBookId(bookId: String): Collection<BookContributorEntityDto> =
    dsl.select(*TableBookContributor.fields(), *TableContributorRole.fields(), *TablePerson.fields())
      .from(TableBookContributor)
      .leftJoin(TableContributorRole)
      .on(TableContributorRole.ID.eq(TableBookContributor.ROLE_ID))
      .leftJoin(TablePerson)
      .on(TablePerson.ID.eq(TableBookContributor.PERSON_ID))
      .where(TableBookContributor.BOOK_ID.eq(bookId))
      .orderBy(TableBookContributor.CREATED_AT.asc())
      .fetch { record ->
        val domain = record.into(TableBookContributor).toDomain()

        domain.toDto().copy(
          attributes = domain.toAttributesDto(
            roleId = record[TableContributorRole.ID],
            roleName = record[TableContributorRole.NAME],
            personId = record[TablePerson.ID],
            personName = record[TablePerson.NAME],
          ),
        )
      }
      .withPictureIfExists()

  @Transactional
  override fun insert(bookContributor: BookContributor) {
    dsl.insertInto(TableBookContributor)
      .set(TableBookContributor.ID, bookContributor.id)
      .set(TableBookContributor.BOOK_ID, bookContributor.bookId)
      .set(TableBookContributor.ROLE_ID, bookContributor.roleId)
      .set(TableBookContributor.PERSON_ID, bookContributor.personId)
      .set(TableBookContributor.CREATED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .execute()
  }

  override fun insert(bookContributors: Collection<BookContributor>) {
    dsl.insertInto(TableBookContributor)
      .columns(
        TableBookContributor.ID,
        TableBookContributor.BOOK_ID,
        TableBookContributor.ROLE_ID,
        TableBookContributor.PERSON_ID,
        TableBookContributor.CREATED_AT,
      )
      .apply {
        bookContributors.forEach { contributor ->
          values(
            contributor.id,
            contributor.bookId,
            contributor.roleId,
            contributor.personId,
            LocalDateTime.now(ZoneId.of("Z")),
          )
        }
      }
      .execute()
  }

  @Transactional
  override fun delete(bookContributorId: String) {
    dsl.deleteFrom(TableBookContributor)
      .where(TableBookContributor.ID.eq(bookContributorId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableBookContributor).execute()
  }

  @Transactional
  override fun deleteAllByBookId(bookId: String) {
    dsl.deleteFrom(TableBookContributor)
      .where(TableBookContributor.BOOK_ID.eq(bookId))
      .execute()
  }

  private fun BookContributorRecord.toDomain() = BookContributor(
    id = id,
    bookId = bookId,
    personId = personId,
    roleId = roleId,
  )

  private fun BookContributorEntityDto.withPictureIfExists(): BookContributorEntityDto {
    val personId = attributes.person.id

    if (!personPictureLifecycle.hasImage(personId)) {
      return this
    }

    return copy(
      relationships = relationships.orEmpty() + listOf(RelationDto(id = personId, type = ReferenceExpansionBookContributor.PERSON_PICTURE)),
    )
  }

  private fun List<BookContributorEntityDto>.withPictureIfExists(): List<BookContributorEntityDto> {
    val entitiesWithImages = personPictureLifecycle.getEntitiesWithImages(map { it.attributes.person.id })

    if (entitiesWithImages.isEmpty()) {
      return this
    }

    return map {
      it.copy(
        relationships = it.relationships.orEmpty() + listOfNotNull(
          RelationDto(id = it.attributes.person.id, type = ReferenceExpansionBookContributor.PERSON_PICTURE)
            .takeIf { relation -> entitiesWithImages.getOrDefault(relation.id, false) },
        ),
      )
    }
  }
}
