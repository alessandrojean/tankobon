package io.github.alessandrojean.tankobon.domain.model

data class Preference(
  val userId: String,
  val key: String,
  val value: String,
)
