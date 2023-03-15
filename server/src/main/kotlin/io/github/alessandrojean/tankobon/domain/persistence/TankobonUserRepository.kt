package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TankobonUserRepository {
  fun findByIdOrNull(userId: String): TankobonUser?
  fun findByEmailIgnoreCaseOrNull(email: String): TankobonUser?

  fun findAll(): Collection<TankobonUser>
  fun findAll(pageable: Pageable): Page<TankobonUser>
  fun findAllByIds(userIds: Collection<String>): Collection<TankobonUser>

  fun existsByEmailIgnoreCase(email: String): Boolean

  fun insert(user: TankobonUser)
  fun update(user: TankobonUser)

  fun delete(userId: String)
  fun deleteAll()

  fun count(): Long
}
