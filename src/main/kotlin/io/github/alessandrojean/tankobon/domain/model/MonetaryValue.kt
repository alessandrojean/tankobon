package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class MonetaryValue(
  val currency: String,
  val value: Float,
) : Serializable
