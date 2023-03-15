package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.datasource.SqliteUdfDataSource
import io.github.alessandrojean.tankobon.jooq.tables.records.UserRecord
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
import io.github.alessandrojean.tankobon.jooq.Tables.USER as TableUser

@Component
class TankobonUserDao(
  private val dsl: DSLContext,
) : TankobonUserRepository {

  private val sorts = mapOf(
    "name" to TableUser.NAME.collate(SqliteUdfDataSource.collationUnicode3),
    "createdAt" to TableUser.CREATED_AT,
    "modifiedAt" to TableUser.MODIFIED_AT,
  )
  
  override fun findByIdOrNull(userId: String): TankobonUser? =
    dsl.selectFrom(TableUser)
      .where(TableUser.ID.equal(userId))
      .fetchOneInto(TableUser)
      ?.toDomain()

  override fun findByEmailIgnoreCaseOrNull(email: String): TankobonUser? =
    dsl.selectFrom(TableUser)
      .where(TableUser.EMAIL.equalIgnoreCase(email))
      .fetchOneInto(TableUser)
      ?.toDomain()

  override fun findAll(): Collection<TankobonUser> =
    dsl.selectFrom(TableUser)
      .fetchInto(TableUser)
      .map { it.toDomain() }

  override fun findAll(pageable: Pageable): Page<TankobonUser> {
    val count = dsl.fetchCount(TableUser)
    val orderBy = pageable.sort.toOrderBy(sorts)

    val users = dsl.selectFrom(TableUser)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableUser)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()

    return PageImpl(
      users,
      if (pageable.isPaged) PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      else PageRequest.of(0, maxOf(count, 20), pageSort),
      count.toLong(),
    )
  }

  override fun findAllByIds(userIds: Collection<String>): Collection<TankobonUser> =
    dsl.selectFrom(TableUser)
      .where(TableUser.ID.`in`(userIds))
      .fetchInto(TableUser)
      .map { it.toDomain() }

  private fun UserRecord.toDomain(): TankobonUser = TankobonUser(
    email = email,
    password = password,
    isAdmin = isAdmin,
    name = name,
    id = id,
    biography = biography,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

  override fun existsByEmailIgnoreCase(email: String): Boolean =
    dsl.fetchExists(
      dsl.selectFrom(TableUser)
        .where(TableUser.EMAIL.equalIgnoreCase(email))
    )

  @Transactional
  override fun insert(user: TankobonUser) {
    dsl.insertInto(TableUser)
      .set(TableUser.ID, user.id)
      .set(TableUser.EMAIL, user.email)
      .set(TableUser.PASSWORD, user.password)
      .set(TableUser.IS_ADMIN, user.isAdmin)
      .set(TableUser.NAME, user.name)
      .set(TableUser.BIOGRAPHY, user.biography)
      .execute()
  }

  @Transactional
  override fun update(user: TankobonUser) {
    dsl.update(TableUser)
      .set(TableUser.EMAIL, user.email)
      .set(TableUser.PASSWORD, user.password)
      .set(TableUser.IS_ADMIN, user.isAdmin)
      .set(TableUser.NAME, user.name)
      .set(TableUser.BIOGRAPHY, user.biography)
      .set(TableUser.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableUser.ID.eq(user.id))
      .execute()
  }

  @Transactional
  override fun delete(userId: String) {
    dsl.deleteFrom(TableUser).where(TableUser.ID.equal(userId)).execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableUser).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableUser).toLong()
}