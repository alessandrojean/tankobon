package io.github.alessandrojean.tankobon.domain.model

open class BookSearch(
  val libraryIds: kotlin.collections.Collection<String>? = null,
  val seriesIds: kotlin.collections.Collection<String>? = null,
  val publisherIds: kotlin.collections.Collection<String>? = null,
  val userId: String? = null,
  val searchTerm: String? = null,
)
