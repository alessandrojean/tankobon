package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.CantChangePasswordInDemoModeException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PasswordUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RoleDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.hibernate.validator.constraints.UUID
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "User", description = "Operations regarding users")
class UserController(
  private val userLifecycle: TankobonUserLifecycle,
  private val userRepository: TankobonUserRepository,
  env: Environment
) {

  private val isDemo = env.activeProfiles.contains("demo")

  @GetMapping("me")
  @Operation(summary = "Get the current authenticated user")
  fun getMe(@AuthenticationPrincipal principal: TankobonPrincipal): SuccessEntityResponseDto<UserEntityDto> =
    SuccessEntityResponseDto(principal.toDto())

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Change the password of the current authenticated user")
  fun updateMyPassword(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto
  ) {
    if (isDemo) {
      throw CantChangePasswordInDemoModeException()
    }

    val user = userRepository.findByEmailIgnoreCaseOrNull(principal.username)
      ?: throw IdDoesNotExistException("User not found")

    userLifecycle.updatePassword(user, newPasswordDto.password, false)
  }

  @GetMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "List all users")
  fun getAllUsers(): SuccessCollectionResponseDto<UserEntityDto> =
    SuccessCollectionResponseDto(userRepository.findAll().map { it.toDto() })

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "Create a new user")
  fun addOneUser(@Valid @RequestBody newUser: UserCreationDto): SuccessEntityResponseDto<UserEntityDto> {
    val user = userLifecycle.createUser(newUser.toDomain())

    return SuccessEntityResponseDto(user.toDto())
  }

  @DeleteMapping("{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Delete a user by its id")
  fun deleteOneUser(
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @AuthenticationPrincipal principal: TankobonPrincipal,
  ) {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    userLifecycle.deleteUser(user)
  }

  @PutMapping("{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Modify a user by its id")
  fun updateUser(
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @Valid @RequestBody
    user: UserUpdateDto,
    @AuthenticationPrincipal principal: TankobonPrincipal
  ) {
    val existing = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val toUpdate = existing.copy(
      email = user.email,
      isAdmin = user.roles.contains(RoleDto.ROLE_ADMIN)
    )

    userLifecycle.updateUser(toUpdate)
  }

  @PatchMapping("{userId}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #id")
  @Operation(summary = "Change the password of a user by its id")
  fun updatePassword(
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto,
  ) {
    if (isDemo) {
      throw CantChangePasswordInDemoModeException()
    }

    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    userLifecycle.updatePassword(user, newPasswordDto.password, user.id != principal.user.id)
  }
}
