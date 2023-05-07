package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Preference
import io.github.alessandrojean.tankobon.domain.persistence.PreferenceRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.UserPreferenceRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import io.github.alessandrojean.tankobon.jooq.Tables.USER_PREFERENCE as TableUserPreference

@Component
class PreferenceDao(
  private val dsl: DSLContext,
) : PreferenceRepository {

  override fun findAllByUser(userId: String): Collection<Preference> =
    dsl.selectFrom(TableUserPreference)
      .where(TableUserPreference.USER_ID.eq(userId))
      .fetchInto(TableUserPreference)
      .map { it.toDomain() }

  override fun findByKeyFromUser(key: String, userId: String): Preference? =
    dsl.selectFrom(TableUserPreference)
      .where(TableUserPreference.USER_ID.eq(userId))
      .and(TableUserPreference.KEY.eq(key))
      .fetchOne()
      ?.toDomain()

  override fun findByKeysFromUser(keys: Set<String>, userId: String): Collection<Preference> =
    dsl.selectFrom(TableUserPreference)
      .where(TableUserPreference.USER_ID.eq(userId))
      .and(TableUserPreference.KEY.`in`(keys))
      .orderBy(TableUserPreference.KEY.sortByValues(keys.toList(), true))
      .fetchInto(TableUserPreference)
      .map { it.toDomain() }

  override fun existsByKeyFromUser(key: String, userId: String): Boolean =
    dsl.fetchExists(
      dsl.select(TableUserPreference.KEY)
        .from(TableUserPreference)
        .where(TableUserPreference.KEY.eq(key))
        .and(TableUserPreference.USER_ID.eq(userId))
    )

  @Transactional
  override fun insert(preference: Preference) {
    dsl.insertInto(TableUserPreference)
      .set(TableUserPreference.USER_ID, preference.userId)
      .set(TableUserPreference.KEY, preference.key)
      .set(TableUserPreference.VALUE, preference.value)
      .execute()
  }

  @Transactional
  override fun update(preference: Preference) {
    dsl.update(TableUserPreference)
      .set(TableUserPreference.VALUE, preference.value)
      .where(TableUserPreference.USER_ID.eq(preference.userId))
      .and(TableUserPreference.KEY.eq(preference.key))
      .execute()
  }

  @Transactional
  override fun insertOrUpdate(preference: Preference) {
    if (existsByKeyFromUser(preference.key, preference.userId)) {
      update(preference)
    } else {
      insert(preference)
    }
  }

  @Transactional
  override fun insertOrUpdate(preferences: Collection<Preference>, userId: String) {
    val preferencesMap = preferences.associate { it.key to it.value }
    val existing = findByKeysFromUser(preferences.map(Preference::key).toSet(), userId).map(Preference::key)
    val nonExisting = preferencesMap.keys.filter { it !in existing }

    dsl.batched { context ->
      existing.forEach { key ->
        context.dsl().update(TableUserPreference)
          .set(TableUserPreference.VALUE, preferencesMap[key])
          .where(TableUserPreference.USER_ID.eq(userId))
          .and(TableUserPreference.KEY.eq(key))
          .execute()
      }
    }

    dsl.insertInto(TableUserPreference, *TableUserPreference.fields())
      .apply { nonExisting.forEach { key -> values(userId, key, preferencesMap[key]) } }
      .execute()
  }

  @Transactional
  override fun delete(key: String, userId: String) {
    dsl.deleteFrom(TableUserPreference)
      .where(TableUserPreference.USER_ID.eq(userId))
      .and(TableUserPreference.KEY.eq(key))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableUserPreference).execute()
  }

  @Transactional
  override fun deleteAllByUser(userId: String) {
    dsl.deleteFrom(TableUserPreference)
      .where(TableUserPreference.USER_ID.eq(userId))
      .execute()
  }

  override fun count(): Long = dsl.fetchCount(TableUserPreference).toLong()

  private fun UserPreferenceRecord.toDomain() = Preference(
    userId = userId,
    key = key,
    value = value,
  )
}