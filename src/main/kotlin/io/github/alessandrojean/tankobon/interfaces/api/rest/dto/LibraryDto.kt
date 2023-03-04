package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.Library
import jakarta.annotation.Nullable
import jakarta.validation.constraints.NotBlank

data class LibraryDto(
  val id: String,
  val name: String,
  val description: String,
  val sharedUsersIds: Set<String> = emptySet(),
)

fun Library.toDto(showSharedUsersIds: Boolean) = LibraryDto(
  id = id,
  name = name,
  description = description,
  sharedUsersIds = if (showSharedUsersIds) sharedUsersIds else emptySet()
)

data class LibraryCreationDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:Nullable val ownerId: String? = null
)

data class LibraryUpdateDto(
  @get:NotBlank val name: String,
  val description: String,
  @get:NotBlank val ownerId: String,
  val sharedUsersIds: Set<String> = emptySet()
)
