package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.TankobonUser

interface TankobonUserRepository {
  fun findByIdOrNull(userId: String): TankobonUser?
  fun findByEmailIgnoreCaseOrNull(email: String): TankobonUser?

  fun findAll(): Collection<TankobonUser>
  fun findAllByIds(userIds: Collection<String>): Collection<TankobonUser>

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun insert(user: TankobonUser)
  fun update(user: TankobonUser)

  fun delete(userId: String)
  fun deleteAll()

  fun count(): Long
}
