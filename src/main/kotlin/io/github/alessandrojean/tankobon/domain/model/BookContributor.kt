package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class BookContributor(
  val bookId: String = "",
  val personId: String = "",
  val roleId: String = "",
) : Serializable
