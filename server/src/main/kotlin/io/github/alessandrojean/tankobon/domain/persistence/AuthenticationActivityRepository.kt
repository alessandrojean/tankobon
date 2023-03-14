package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.AuthenticationActivity
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface AuthenticationActivityRepository {
  fun findAll(pageable: Pageable): Page<AuthenticationActivity>
  fun findAllByUser(user: TankobonUser, pageable: Pageable): Page<AuthenticationActivity>
  fun findMostRecentByUser(user: TankobonUser): AuthenticationActivity?

  fun insert(activity: AuthenticationActivity)

  fun deleteByUser(user: TankobonUser)
  fun deleteOlderThan(dateTime: LocalDateTime)
}
