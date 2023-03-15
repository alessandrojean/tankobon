package io.github.alessandrojean.tankobon.interfaces.api.rest

import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.Preference
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.persistence.PreferenceRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.infrastructure.security.TankobonPrincipal
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PreferenceCreationUpdateDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PreferenceEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessCollectionResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.SuccessEntityResponseDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.toDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.tags.Tag as SwaggerTag

@Validated
@RestController
@RequestMapping("api", produces = [MediaType.APPLICATION_JSON_VALUE])
@SwaggerTag(name = "Preference", description = "Operations user preferences")
class PreferenceController(
  private val userRepository: TankobonUserRepository,
  private val preferenceRepository: PreferenceRepository,
) {

  @GetMapping("v1/users/me/preferences")
  @Operation(
    summary = "Get all preferences from the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun getAllMePreferences(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable @UUID(version = [4]) @Schema(format = "uuid") userId: String,
  ): SuccessCollectionResponseDto<PreferenceEntityDto> {
    val preferences = preferenceRepository
      .findAllByUser(principal.user.id)
      .map { it.toDto() }

    return SuccessCollectionResponseDto(preferences)
  }

  @GetMapping("v1/users/{userId}/preferences")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or authentication.principal.user.id == #userId")
  @Operation(summary = "Get all preferences from a user", security = [SecurityRequirement(name = "Basic Auth")])
  fun getAllPreferencesByUser(
    @PathVariable("userId") @UUID(version = [4]) @Schema(format = "uuid") userId: String,
  ): SuccessCollectionResponseDto<PreferenceEntityDto> {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    val preferences = preferenceRepository
      .findAllByUser(user.id)
      .map { it.toDto() }

    return SuccessCollectionResponseDto(preferences)
  }

  @PostMapping("v1/users/me/preferences")
  @Operation(
    summary = "Set a preference value by key to the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun setMePreferenceValueByKey(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @Valid @RequestBody preference: PreferenceCreationUpdateDto,
  ): SuccessEntityResponseDto<PreferenceEntityDto> {
    val exists = preferenceRepository.existsByKeyFromUser(preference.key, principal.user.id)
    val preferenceDomain = Preference(
      userId = principal.user.id,
      key = preference.key,
      value = preference.value,
    )

    if (exists) {
      preferenceRepository.update(preferenceDomain)
    } else {
      preferenceRepository.insert(preferenceDomain)
    }

    return SuccessEntityResponseDto(preferenceDomain.toDto())
  }

  @PostMapping("v1/users/{userId}/preferences")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or authentication.principal.user.id == #userId")
  @Operation(summary = "Set a preference value by key to a user", security = [SecurityRequirement(name = "Basic Auth")])
  fun setPreferenceValueByKey(
    @PathVariable("userId") @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @Valid @RequestBody preference: PreferenceCreationUpdateDto,
  ): SuccessEntityResponseDto<PreferenceEntityDto> {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw RelationIdDoesNotExistException("User not found")

    val exists = preferenceRepository.existsByKeyFromUser(preference.key, user.id)
    val preferenceDomain = Preference(
      userId = user.id,
      key = preference.key,
      value = preference.value,
    )

    if (exists) {
      preferenceRepository.update(preferenceDomain)
    } else {
      preferenceRepository.insert(preferenceDomain)
    }

    return SuccessEntityResponseDto(preferenceDomain.toDto())
  }

  @DeleteMapping("v1/users/me/preferences/{preferenceKey:.+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Delete a preference by key from the current authenticated user",
    security = [SecurityRequirement(name = "Basic Auth")]
  )
  fun deletePreferenceByKeyFromMe(
    @AuthenticationPrincipal principal: TankobonPrincipal,
    @PathVariable preferenceKey: String,
  ) {
    if (!preferenceRepository.existsByKeyFromUser(preferenceKey, principal.user.id)) {
      throw IdDoesNotExistException("Preference not found")
    }

    preferenceRepository.delete(preferenceKey, principal.user.id)
  }

  @DeleteMapping("v1/users/{userId}/preferences/{preferenceKey:.+}")
  @PreAuthorize("hasRole('$ROLE_ADMIN') or authentication.principal.user.id == #userId")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a preference by key from a user", security = [SecurityRequirement(name = "Basic Auth")])
  fun deletePreferenceByKeyFromUser(
    @PathVariable("userId") @UUID(version = [4]) @Schema(format = "uuid") userId: String,
    @PathVariable("preferenceKey") preferenceKey: String,
  ) {
    val user = userRepository.findByIdOrNull(userId)
      ?: throw IdDoesNotExistException("User not found")

    if (!preferenceRepository.existsByKeyFromUser(preferenceKey, user.id)) {
      throw IdDoesNotExistException("Preference not found")
    }

    preferenceRepository.delete(preferenceKey, user.id)
  }
}
