package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.util.UUID

data class BookContributor(
  val id: String = UUID.randomUUID().toString(),
  val bookId: String = "",
  val personId: String = "",
  val roleId: String = "",
) : Serializable
