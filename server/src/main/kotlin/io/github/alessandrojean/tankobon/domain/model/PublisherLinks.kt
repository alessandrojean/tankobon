package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class PublisherLinks(
  val website: String? = null,
  val store: String? = null,
  val twitter: String? = null,
  val instagram: String? = null,
  val facebook: String? = null,
  val youTube: String? = null,
) : Serializable
