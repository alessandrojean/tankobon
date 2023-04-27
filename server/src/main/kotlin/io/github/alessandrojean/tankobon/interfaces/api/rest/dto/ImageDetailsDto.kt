package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ImageDetails

data class ImageDetailsAttributesDto(
  val fileName: String,
  val versions: Map<String, String> = emptyMap(),
  val width: Int,
  val height: Int,
  val aspectRatio: String,
  val format: String,
  val mimeType: String,
  val timeHex: String,
  val blurHash: String,
) : EntityAttributesDto()

fun ImageDetails.toAttributesDto() = ImageDetailsAttributesDto(
  fileName = fileName,
  versions = versions,
  width = width,
  height = height,
  aspectRatio = aspectRatio,
  format = format,
  mimeType = mimeType,
  timeHex = timeHex,
  blurHash = blurHash,
)
