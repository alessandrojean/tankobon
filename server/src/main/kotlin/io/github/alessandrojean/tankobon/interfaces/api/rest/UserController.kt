package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.CantChangeDemoUserAttributesException
import io.github.alessandrojean.tankobon.domain.model.CantChangePasswordInDemoModeException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.AuthenticationActivityRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.ReferenceExpansion
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.UserAvatarLifecycle
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.infrastructure.validation.SupportedImageFormat
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.AuthenticationActivityEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PasswordUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RoleDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessPaginatedCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.UserUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toSuccessCollectionResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.hibernate.validator.constraints.UUID
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.core.env.Environment
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("api/v1/users", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "User", description = "Operations regarding users")
class UserController(
  private val userLifecycle: TankobonUserLifecycle,
  private val userRepository: TankobonUserRepository,
  private val authenticationActivityRepository: AuthenticationActivityRepository,
  private val userAvatarLifecycle: UserAvatarLifecycle,
  private val referenceExpansion: ReferenceExpansion,
  env: Environment
) {

  private val isDemo = env.activeProfiles.contains("demo")

  @GetMapping("me")
  @Operation(summary = "Get the current authenticated user", security = [SecurityRequirement(name = "Basic Auth")])
  fun getMe(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<UserEntityDto> {
    val current = principal.toDto().withAvatarIfExists()
    val expanded = referenceExpansion.expand(current, includes)

    return SuccessEntityResponseDto(expanded)
  }

  @PutMapping("me")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Modify the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun updateMe(
    @Valid @RequestBody
    user: UserUpdateDto,
    @AuthenticationPrincipal principal: TankobonPrincipal
  ) {
    if (isDemo) {
      throw CantChangeDemoUserAttributesException()
    }

    val existing = userRepository.findByIdOrNull(principal.user.id)
      ?: throw IdDoesNotExistException("User not found")

    val toUpdate = existing.copy(
      name = user.name,
      biography = user.biography,
      email = user.email,
      isAdmin = user.roles.contains(RoleDto.ROLE_ADMIN)
    )

    userLifecycle.updateUser(toUpdate)
  }

  @PostMapping("me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Upload an avatar to the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun uploadMeAvatar(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam("avatar") @SupportedImageFormat avatarFile: MultipartFile,
  ) {
    if (isDemo) {
      throw CantChangeDemoUserAttributesException()
    }

    userAvatarLifecycle.createAvatar(principal.user.id, avatarFile.bytes)
  }

  @DeleteMapping("me/avatar")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Delete the avatar of the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun deleteMeAvatar(
    @AuthenticationPrincipal principal: TankobonPrincipal,
  ) {
    if (isDemo) {
      throw CantChangeDemoUserAttributesException()
    }

    val existing = userRepository.findByIdOrNull(principal.user.id)
      ?: throw IdDoesNotExistException("User not found")

    userAvatarLifecycle.deleteAvatar(existing.id)
  }

  @PatchMapping("me/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Change the password of the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
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

  @GetMapping("me/authentication-activity")
  @PageableAsQueryParam
  @Operation(
    summary = "Get the authentication activity of the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getMyAuthenticationActivity(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<AuthenticationActivityEntityDto> {
    if (isDemo && !principal.user.isAdmin) {
      throw UserDoesNotHaveAccessException()
    }

    val sort = if (page.sort.isSorted) {
      page.sort
    } else {
      Sort.by(Sort.Order.desc("timestamp"))
    }

    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      sort,
    )

    val activity = authenticationActivityRepository
      .findAllByUser(principal.user, pageRequest)
      .map { it.toDto() }
    val expanded = referenceExpansion.expand(activity.content, includes)

    return PageImpl(expanded, activity.pageable, activity.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping("authentication-activity")
  @PageableAsQueryParam
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(
    summary = "Get the authentication activities",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getAuthenticationActivity(
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<AuthenticationActivityEntityDto> {
    val sort = if (page.sort.isSorted) {
      page.sort
    } else {
      Sort.by(Sort.Order.desc("timestamp"))
    }

    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      sort,
    )

    val activity = authenticationActivityRepository
      .findAll(pageRequest)
      .map { it.toDto() }
    val expanded = referenceExpansion.expand(activity.content, includes)

    return PageImpl(expanded, activity.pageable, activity.totalElements)
      .toSuccessCollectionResponseDto { it }
  }

  @GetMapping
  @PageableAsQueryParam
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "List all users", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllUsers(
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
    @Parameter(hidden = true) page: Pageable,
  ): SuccessPaginatedCollectionResponseDto<UserEntityDto> {
    val sort = when {
      page.sort.isSorted -> page.sort
      else -> Sort.by(Sort.Order.desc("createdAt"))
    }

    val pageRequest = PageRequest.of(
      page.pageNumber,
      page.pageSize,
      sort,
    )

    val users = userRepository.findAll(pageRequest).map { it.toDto().withAvatarIfExists() }
    val expanded = referenceExpansion.expand(users.content, includes)

    return PageImpl(expanded, users.pageable, users.totalElements)
      .toSuccessCollectionResponseDto()
  }

  @GetMapping("{userId}")
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "Get a user by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun getOneUser(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) userId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<UserEntityDto> {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val current = user.toDto().withAvatarIfExists()
    val expanded = referenceExpansion.expand(current, includes)

    return SuccessEntityResponseDto(expanded)
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('$ROLE_ADMIN')")
  @Operation(summary = "Create a new user", security = [SecurityRequirement(name = "Basic Auth")])
  fun addOneUser(@Valid @RequestBody newUser: UserCreationDto): SuccessEntityResponseDto<UserEntityDto> {
    val user = userLifecycle.createUser(newUser.toDomain())

    return SuccessEntityResponseDto(user.toDto())
  }

  @DeleteMapping("{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Delete a user by its id", security = [SecurityRequirement(name = "Basic Auth")])
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
  @Operation(summary = "Modify a user by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun updateUser(
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @Valid @RequestBody
    user: UserUpdateDto,
    @AuthenticationPrincipal principal: TankobonPrincipal
  ) {
    if (isDemo && userId == principal.user.id) {
      throw CantChangeDemoUserAttributesException()
    }

    val existing = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val toUpdate = existing.copy(
      email = user.email,
      isAdmin = user.roles.contains(RoleDto.ROLE_ADMIN)
    )

    userLifecycle.updateUser(toUpdate)
  }

  @PostMapping("{userId}/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Upload an avatar to a user by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun uploadUserAvatar(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @RequestParam("avatar") @SupportedImageFormat avatarFile: MultipartFile,
  ) {
    if (isDemo && userId == principal.user.id) {
      throw CantChangeDemoUserAttributesException()
    }

    val existing = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    userAvatarLifecycle.createAvatar(existing.id, avatarFile.bytes)
  }

  @DeleteMapping("{userId}/avatar")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') and #principal.user.id != #id")
  @Operation(summary = "Delete an user avatar by its id", security = [SecurityRequirement(name = "Basic Auth")])
  fun deleteUserAvatar(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String
  ) {
    val existing = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    userAvatarLifecycle.deleteAvatar(existing.id)
  }

  @PatchMapping("{userId}/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #userId")
  @Operation(
    summary = "Change the password of a user by its id",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
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

  private fun UserEntityDto.withAvatarIfExists(): UserEntityDto {
    if (!userAvatarLifecycle.hasAvatar(id)) {
      return this
    }

    return copy(
      relationships = listOf(RelationDto(id = id, type = RelationshipType.AVATAR))
    )
  }

  @GetMapping("{userId}/authentication-activity/latest")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or #principal.user.id == #userId")
  @Operation(
    summary = "Get the latest authentication activity of a user by its id",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getLatestAuthenticationActivityForUser(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) userId: String,
    @RequestParam(required = false, defaultValue = "") includes: Set<RelationshipType> = emptySet(),
  ): SuccessEntityResponseDto<AuthenticationActivityEntityDto> {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val activity = authenticationActivityRepository.findMostRecentByUser(user)?.toDto()
      ?: throw IdDoesNotExistException("Activity not found")
    val expanded = referenceExpansion.expand(activity, includes)

    return SuccessEntityResponseDto(expanded)
  }
}
