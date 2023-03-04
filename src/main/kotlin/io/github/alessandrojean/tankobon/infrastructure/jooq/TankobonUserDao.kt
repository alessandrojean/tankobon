package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.jooq.tables.records.UserRecord
import io.github.alessandrojean.tankobon.jooq.Tables.USER as TableUser
import io.github.alessandrojean.tankobon.jooq.Tables.USER_LIBRARY_SHARING as TableUserLibrarySharing
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.ResultQuery
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class TankobonUserDao(
  private val dsl: DSLContext,
) : TankobonUserRepository {
  
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

  private fun UserRecord.toDomain(): TankobonUser = TankobonUser(
    email = email,
    password = password,
    isAdmin = isAdmin,
    id = id,
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
      .execute()
  }

  @Transactional
  override fun update(user: TankobonUser) {
    dsl.update(TableUser)
      .set(TableUser.EMAIL, user.email)
      .set(TableUser.PASSWORD, user.password)
      .set(TableUser.IS_ADMIN, user.isAdmin)
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