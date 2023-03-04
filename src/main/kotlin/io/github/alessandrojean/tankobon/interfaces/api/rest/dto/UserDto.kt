package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.ROLE_USER
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import kotlin.properties.Delegates

data class UserDto(
  val id: String,
  val email: String,
  val roles: Set<String> = emptySet(),
)

fun TankobonUser.toDto() = UserDto(
  id = id,
  email = email,
  roles = roles
)

fun TankobonPrincipal.toDto() = user.toDto()

data class UserCreationDto(
  @get:Email(regexp = ".+@.+\\..+") val email: String,
  @get:NotBlank val password: String,
  val roles: List<String>,
) {
  fun toDomain(): TankobonUser = TankobonUser(
    email,
    password,
    isAdmin = roles.contains(ROLE_ADMIN)
  )
}

data class PasswordUpdateDto(
  @get:NotBlank val password: String,
)

data class UserUpdateDto(
  @get:Email(regexp = ".+@.+\\..+") val email: String,
  val roles: List<String>,
)
