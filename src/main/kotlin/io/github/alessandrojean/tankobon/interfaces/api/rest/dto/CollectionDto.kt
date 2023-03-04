package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Collection

data class CollectionDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Collection.toDto(): CollectionDto = CollectionDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class CollectionCreationDto(
  val name: String,
  val description: String,
)

data class CollectionUpdateDto(
  val name: String,
  val description: String,
)
