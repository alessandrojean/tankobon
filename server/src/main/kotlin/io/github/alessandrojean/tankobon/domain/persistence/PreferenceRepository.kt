package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Preference

interface PreferenceRepository {
  fun findAllByUser(userId: String): Collection<Preference>
  fun findByKeyFromUser(key: String, userId: String): Preference?
  fun findByKeysFromUser(keys: Set<String>, userId: String): Collection<Preference>

  fun existsByKeyFromUser(key: String, userId: String): Boolean

  fun insert(preference: Preference)
  fun update(preference: Preference)

  fun insertOrUpdate(preference: Preference)
  fun insertOrUpdate(preferences: Collection<Preference>, userId: String)

  fun delete(key: String, userId: String)
  fun deleteAll()
  fun deleteAllByUser(userId: String)

  fun count(): Long
}
