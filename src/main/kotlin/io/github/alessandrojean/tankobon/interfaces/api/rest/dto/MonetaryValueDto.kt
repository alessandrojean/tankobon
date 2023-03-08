package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern

data class MonetaryValueDto(
  @get:Pattern(regexp = "^[A-Z]{3}$")
  @get:Schema(description = "ISO 4127 currency code")
  val currency: String,
  val value: Float,
)
