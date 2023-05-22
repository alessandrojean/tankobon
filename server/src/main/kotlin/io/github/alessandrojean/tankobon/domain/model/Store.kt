package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class Store(
  val name: String,

  val id: String = UUID.randomUUID().toString(),
  override val libraryId: String = "",
  val description: String = "",
  val links: StoreLinks = StoreLinks(),
  val legalName: String = "",
  val location: String? = null,
  val type: StoreType? = null,

  override val createdAt: LocalDateTime = LocalDateTime.now(),
  override val modifiedAt: LocalDateTime = createdAt
) : LibraryItem, Auditable, Serializable
