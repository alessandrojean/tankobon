package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Tag

data class TagDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Tag.toDto(): TagDto = TagDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class TagCreationDto(
  val name: String,
  val description: String,
)

data class TagUpdateDto(
  val name: String,
  val description: String,
)
