package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.AuthenticationActivity
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.AuthenticationActivityRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.AuthenticationActivityRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import io.github.alessandrojean.tankobon.jooq.Tables.AUTHENTICATION_ACTIVITY as TableAuthenticationActivity

@Component
class AuthenticationActivityDao(
  private val dsl: DSLContext,
) : AuthenticationActivityRepository {

  private val sorts = mapOf(
    "timestamp" to TableAuthenticationActivity.TIMESTAMP,
    "email" to TableAuthenticationActivity.EMAIL,
    "success" to TableAuthenticationActivity.SUCCESS,
    "ip" to TableAuthenticationActivity.IP,
    "error" to TableAuthenticationActivity.ERROR,
    "userId" to TableAuthenticationActivity.USER_ID,
    "userAgent" to TableAuthenticationActivity.USER_AGENT,
  )

  override fun findAll(pageable: Pageable): Page<AuthenticationActivity> {
    val conditions: Condition = DSL.trueCondition()
    return findAll(conditions, pageable)
  }

  override fun findAllByUser(user: TankobonUser, pageable: Pageable): Page<AuthenticationActivity> {
    val conditions = TableAuthenticationActivity.USER_ID.eq(user.id)
      .or(TableAuthenticationActivity.EMAIL.eq(user.email))

    return findAll(conditions, pageable)
  }

  override fun findMostRecentByUser(user: TankobonUser): AuthenticationActivity? =
    dsl.selectFrom(TableAuthenticationActivity)
      .where(TableAuthenticationActivity.USER_ID.eq(user.id))
      .or(TableAuthenticationActivity.EMAIL.eq(user.email))
      .orderBy(TableAuthenticationActivity.TIMESTAMP.desc())
      .limit(1)
      .fetchOne()
      ?.toDomain()

  private fun findAll(conditions: Condition, pageable: Pageable): PageImpl<AuthenticationActivity> {
    val count = dsl.fetchCount(TableAuthenticationActivity, conditions)

    val orderBy = pageable.sort.toOrderBy(sorts)

    val items = dsl.selectFrom(TableAuthenticationActivity)
      .where(conditions)
      .orderBy(orderBy)
      .apply { if (pageable.isPaged) limit(pageable.pageSize).offset(pageable.offset) }
      .fetchInto(TableAuthenticationActivity)
      .map { it.toDomain() }

    val pageSort = if (orderBy.isNotEmpty()) pageable.sort else Sort.unsorted()
    return PageImpl(
      items,
      if (pageable.isPaged) {
        PageRequest.of(pageable.pageNumber, pageable.pageSize, pageSort)
      } else {
        PageRequest.of(0, maxOf(count, 20), pageSort)
      },
      count.toLong(),
    )
  }

  override fun insert(activity: AuthenticationActivity) {
    dsl
      .insertInto(
        TableAuthenticationActivity,
        TableAuthenticationActivity.ID,
        TableAuthenticationActivity.USER_ID,
        TableAuthenticationActivity.EMAIL,
        TableAuthenticationActivity.IP,
        TableAuthenticationActivity.USER_AGENT,
        TableAuthenticationActivity.SUCCESS,
        TableAuthenticationActivity.ERROR,
        TableAuthenticationActivity.SOURCE,
      )
      .values(
        activity.id,
        activity.userId,
        activity.email,
        activity.ip,
        activity.userAgent,
        activity.success,
        activity.error,
        activity.source,
      )
      .execute()
  }

  override fun deleteByUser(user: TankobonUser) {
    dsl.deleteFrom(TableAuthenticationActivity)
      .where(TableAuthenticationActivity.USER_ID.eq(user.id))
      .or(TableAuthenticationActivity.EMAIL.eq(user.email))
      .execute()
  }

  override fun deleteOlderThan(dateTime: LocalDateTime) {
    dsl.deleteFrom(TableAuthenticationActivity)
      .where(TableAuthenticationActivity.TIMESTAMP.lt(dateTime))
      .execute()
  }

  private fun AuthenticationActivityRecord.toDomain() =
    AuthenticationActivity(
      userId = userId,
      email = email,
      ip = ip,
      userAgent = userAgent,
      success = success,
      error = error,
      timestamp = timestamp.toCurrentTimeZone(),
      source = source,
    )
}
