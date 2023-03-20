package io.github.alessandrojean.tankobon.domain.model

open class ContributorRoleSearch(
  val libraryIds: kotlin.collections.Collection<String>? = null,
  val userId: String? = null,
  val searchTerm: String? = null,
)
