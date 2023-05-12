package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.AuthenticationActivity
import io.github.alessandrojean.tankobon.infrastructure.jooq.toUtcTimeZone
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AuthenticationActivityEntityDto(
  override val id: String,
  override val attributes: AuthenticationActivityAttributesDto,
  override var relationships: List<RelationDto<ReferenceExpansionAuthenticationActivity>>? = null,
) : EntityDto<ReferenceExpansionAuthenticationActivity> {
  @Schema(type = "string", allowableValues = ["AUTHENTICATION_ACTIVITY"])
  override val type = EntityType.AUTHENTICATION_ACTIVITY
}

data class AuthenticationActivityAttributesDto(
  val email: String? = null,
  val ip: String? = null,
  val userAgent: String? = null,
  val success: Boolean,
  val error: String? = null,
  val timestamp: LocalDateTime = LocalDateTime.now(),
  val source: String? = null,
) : EntityAttributesDto()

enum class ReferenceExpansionAuthenticationActivity : ReferenceExpansionEnum {
  USER
}

fun AuthenticationActivity.toDto(userAttributes: UserAttributesDto? = null) = AuthenticationActivityEntityDto(
  id = id,
  attributes = toAttributesDto(),
  relationships = listOfNotNull(
    userId?.let {
      RelationDto(
        id = userId,
        type = ReferenceExpansionAuthenticationActivity.USER,
        attributes = userAttributes,
      )
    }
  ),
)

fun AuthenticationActivity.toAttributesDto() = AuthenticationActivityAttributesDto(
  email = email,
  ip = ip,
  userAgent = userAgent,
  success = success,
  error = error,
  timestamp = timestamp.toUtcTimeZone(),
  source = source,
)