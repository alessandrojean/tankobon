package io.github.alessandrojean.tankobon.infrastructure.importer

import io.github.alessandrojean.tankobon.domain.model.Dimensions
import javax.money.MonetaryAmount

data class ImporterBookResult(
  val id: String,
  val provider: ImporterSource,
  val isbn: String,
  val title: String,
  val contributors: List<ImporterBookContributor>,
  val publisher: String,
  val number: String = "1",
  val subtitle: String = "",
  val synopsis: String = "",
  val dimensions: Dimensions? = null,
  val labelPrice: MonetaryAmount? = null,
  val coverUrl: String? = null,
  val pageCount: Int = 0,
  val url: String? = null,
)

data class ImporterBookContributor(
  val name: String,
  val role: String,
)
