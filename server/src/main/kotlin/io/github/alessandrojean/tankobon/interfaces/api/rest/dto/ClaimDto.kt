package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ClaimStatusDto(
  val isClaimed: Boolean,
)

data class ClaimDto(
  @get:NotBlank
  val name: String,
  @get:NotBlank
  @get:Email(regexp = ".+@.+\\..+")
  @get:Schema(format = "email")
  val email: String,
  @get:NotBlank
  @get:Schema(format = "password")
  val password: String,
)
