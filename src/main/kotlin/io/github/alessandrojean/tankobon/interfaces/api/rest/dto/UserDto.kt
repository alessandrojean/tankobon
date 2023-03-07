package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserEntityDto(
  override val id: String,
  override val attributes: UserAttributesDto,
  override var relationships: List<RelationDto>? = null,
) : EntityDto {
  override val type = EntityType.USER
}

data class UserAttributesDto(
  val email: String,
  val roles: Set<String> = emptySet(),
) : EntityAttributesDto()

fun TankobonUser.toDto() = UserEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = null,
)

fun TankobonUser.toAttributesDto() = UserAttributesDto(email, roles)

fun TankobonPrincipal.toDto() = user.toDto()

data class UserCreationDto(
  @get:Email(regexp = ".+@.+\\..+") val email: String,
  @get:NotBlank val password: String,
  val roles: List<@NotBlank String>,
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
  val roles: List<@NotBlank String>,
)
