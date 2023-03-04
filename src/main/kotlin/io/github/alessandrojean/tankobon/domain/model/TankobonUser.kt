package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"

data class TankobonUser(
  @Email(regexp = ".+@.+\\..+")
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val isAdmin: Boolean,

  val id: String = UUID.randomUUID().toString(),

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt,
): Auditable, Serializable {

  @delegate:Transient
  val roles: Set<String> by lazy {
    buildSet {
      add(ROLE_USER)
      if (isAdmin) add(ROLE_ADMIN)
    }
  }

  fun canAccessLibrary(library: Library): Boolean =
    library.ownerId == id || isAdmin || library.sharedUsersIds.contains(id)
}
