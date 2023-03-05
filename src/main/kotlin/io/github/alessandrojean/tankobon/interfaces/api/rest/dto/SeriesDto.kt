package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Series

data class SeriesDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Series.toDto(): SeriesDto = SeriesDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class SeriesCreationDto(
  val name: String,
  val description: String,
)

data class SeriesUpdateDto(
  val name: String,
  val description: String,
)
