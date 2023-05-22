package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class UserEntityDto(
  override val id: String,
  override val attributes: UserAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionUser>>? = null,
) : EntityDto<ReferenceExpansionUser> {
  @Schema(type = "string", allowableValues = ["USER"])
  override val type = EntityType.USER
}

data class UserAttributesDto(
  val name: String,
  val biography: String = "",
  val email: String,
  val roles: Set<RoleDto> = emptySet(),
  val createdAt: LocalDateTime,
) : EntityAttributesDto()

enum class RoleDto {
  ROLE_USER, ROLE_ADMIN
}

enum class ReferenceExpansionUser : ReferenceExpansionEnum {
  AVATAR,
}

fun TankobonUser.toDto(avatarRelationship: RelationDto<ReferenceExpansionUser>? = null) = UserEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = avatarRelationship?.let { listOf(it) },
)

fun TankobonUser.toAttributesDto() = UserAttributesDto(
  name = name,
  biography = biography,
  email = email,
  roles = roles.map { RoleDto.valueOf("ROLE_$it") }.toSet(),
  createdAt = createdAt.toUtcTimeZone(),
)

fun TankobonPrincipal.toDto() = user.toDto()

data class UserCreationDto(
  @get:NotBlank
  val name: String,
  @get:Email(regexp = ".+@.+\\..+")
  @get:Schema(format = "email")
  val email: String,
  @get:NotBlank
  @get:Schema(format = "password")
  val password: String,
  val roles: Set<RoleDto>,
) {
  fun toDomain(): TankobonUser = TankobonUser(
    email,
    password,
    isAdmin = roles.contains(RoleDto.ROLE_ADMIN),
    name,
  )
}

data class PasswordUpdateDto(
  @get:NotBlank
  @Schema(format = "password")
  val password: String,
)

data class UserUpdateDto(
  @get:NotBlank val name: String,
  @get:NotNull val biography: String = "",
  @get:Email(regexp = ".+@.+\\..+")
  @get:Schema(format = "email")
  val email: String,
  val roles: Set<RoleDto>,
)

data class EmailAvailabilityDto(val isAvailable: Boolean)
