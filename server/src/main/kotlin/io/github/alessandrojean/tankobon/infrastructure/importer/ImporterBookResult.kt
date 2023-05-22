package io.github.alessandrojean.tankobon.infrastructure.importer

import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookLinksDto
import javax.money.MonetaryAmount

data class ImporterBookResult(
  val id: String,
  val provider: ImporterSource,
  val isbn: String,
  val title: String,
  val contributors: List<ImporterBookContributor>,
  val publisher: String,
  val number: String = "",
  val subtitle: String = "",
  val synopsis: String = "",
  val dimensions: Dimensions? = null,
  val labelPrice: MonetaryAmount? = null,
  val coverUrl: String? = null,
  val pageCount: Int = 0,
  val url: String? = null,
  val weightKg: Float = 0f,
  val links: BookLinksDto = BookLinksDto(),
)

data class ImporterBookContributor(
  val name: String,
  val role: String,
)
