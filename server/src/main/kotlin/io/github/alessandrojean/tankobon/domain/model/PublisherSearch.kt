package io.github.alessandrojean.tankobon.domain.model

open class PublisherSearch(
  val libraryIds: kotlin.collections.Collection<String>? = null,
  val userId: String? = null,
  val searchTerm: String? = null,
)
