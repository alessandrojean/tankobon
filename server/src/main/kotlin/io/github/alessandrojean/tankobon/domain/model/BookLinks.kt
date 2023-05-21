package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class BookLinks(
  val amazon: String? = null,
  val openLibrary: String? = null,
  val skoob: String? = null,
  val goodreads: String? = null,
  val guiaDosQuadrinhos: String? = null,
) : Serializable
