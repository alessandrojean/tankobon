package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Store

data class StoreDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun Store.toDto(): StoreDto = StoreDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class StoreCreationDto(
  val name: String,
  val description: String,
)

data class StoreUpdateDto(
  val name: String,
  val description: String,
)
