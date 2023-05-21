package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class SeriesLinks(
  val website: String? = null,
  val myAnimeList: String? = null,
  val kitsu: String? = null,
  val aniList: String? = null,
  val mangaUpdates: String? = null,
  val guiaDosQuadrinhos: String? = null,
  val twitter: String? = null,
  val instagram: String? = null,
) : Serializable
