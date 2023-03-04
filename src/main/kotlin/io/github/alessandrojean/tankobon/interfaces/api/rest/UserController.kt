package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.UserEmailAlreadyExistsException
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PasswordUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserUpdateDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "users", description = "Operations regarding users")
class UserController(
  private val userLifecycle: TankobonUserLifecycle,
  private val userRepository: TankobonUserRepository,
  env: Environment
) {

  private val isDemo = env.activeProfiles.contains("demo")

  @GetMapping("me")
  @Operation(summary = "Get the current authenticated user")
  fun getMe(@AuthenticationPrincipal principal: TankobonPrincipal): UserDto =
    principal.toDto()

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Change the password of the current authenticated user")
  fun updateMyPassword(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto
  ) {
    if (isDemo) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    userRepository.findByEmailIgnoreCaseOrNull(principal.username)?.let { user ->
      userLifecycle.updatePassword(user, newPasswordDto.password, false)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @GetMapping
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "List all users")
  fun getAll(): List<UserDto> =
    userRepository.findAll().map { it.toDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "Create a new user")
  fun addOne(@Valid @RequestBody newUser: UserCreationDto): UserDto {
    return try {
      userLifecycle.createUser(newUser.toDomain()).toDto()
    } catch (e: UserEmailAlreadyExistsException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with this email already exists")
    }
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Delete an existing user by its id")
  fun delete(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: TankobonPrincipal,
  ) {
    userRepository.findByIdOrNull(id)?.let {
      userLifecycle.deleteUser(it)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PutMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Modify an existing user by its id")
  fun updateUser(
    @PathVariable id: String,
    @Valid @RequestBody
    user: UserUpdateDto,
    @AuthenticationPrincipal principal: TankobonPrincipal
  ) {
    userRepository.findByIdOrNull(id)?.let { existing ->
      val toUpdate = existing.copy(
        email = user.email,
        isAdmin = user.roles.contains(ROLE_ADMIN)
      )

      userLifecycle.updateUser(toUpdate)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }

  @PatchMapping("{id}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #id")
  @Operation(summary = "Change the password of a user by its id")
  fun updatePassword(
    @PathVariable id: String,
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody
    newPasswordDto: PasswordUpdateDto,
  ) {
    if (isDemo) {
      throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    userRepository.findByIdOrNull(id)?.let { user ->
      userLifecycle.updatePassword(user, newPasswordDto.password, user.id != principal.user.id)
    } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
  }
}
