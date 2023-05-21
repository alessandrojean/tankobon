package io.github.alessandrojean.tankobon.domain.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

const val ROLE_USER = "USER"
const val ROLE_ADMIN = "ADMIN"

data class TankobonUser(
  @Email(regexp = ".+@.+\\..+")
  @NotBlank
  val email: String,
  @NotBlank
  val password: String,
  val isAdmin: Boolean,
  @NotBlank
  val name: String,

  val id: String = UUID.randomUUID().toString(),
  val biography: String = "",

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
