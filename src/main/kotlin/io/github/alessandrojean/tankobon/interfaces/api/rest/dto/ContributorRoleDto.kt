package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

import io.github.alessandrojean.tankobon.domain.model.ContributorRole

data class ContributorRoleDto(
  val id: String,
  val name: String,
  val description: String,
  val libraryId: String,
)

fun ContributorRole.toDto(): ContributorRoleDto = ContributorRoleDto(
  id = id,
  name = name,
  description = description,
  libraryId = libraryId,
)

data class ContributorRoleCreationDto(
  val name: String,
  val description: String,
)

data class ContributorRoleUpdateDto(
  val name: String,
  val description: String,
)
