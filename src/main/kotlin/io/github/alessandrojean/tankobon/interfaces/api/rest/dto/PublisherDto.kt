package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Publisher

data class PublisherDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Publisher.toDto(): PublisherDto = PublisherDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class PublisherCreationDto(
  val name: String,
  val description: String,
)

data class PublisherUpdateDto(
  val name: String,
  val description: String,
)
