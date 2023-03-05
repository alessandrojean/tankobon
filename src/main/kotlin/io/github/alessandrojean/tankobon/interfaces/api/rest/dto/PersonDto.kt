package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Person

data class PersonDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Person.toDto(): PersonDto = PersonDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class PersonCreationDto(
  val name: String,
  val description: String,
)

data class PersonUpdateDto(
  val name: String,
  val description: String,
)
