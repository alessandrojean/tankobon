package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

data class ImageDetails(
  val id: String,
  val fileName: String,
  val versions: Map<String, String> = emptyMap(),
  val width: Int,
  val height: Int,
  val aspectRatio: String,
  val format: String,
  val mimeType: String,
  val timeHex: String,
) : Serializable

