package io.github.alessandrojean.tankobon.domain.model

import java.util.UUID

fun makeLibrary(
  name: String = "default",
  description: String = "Default",
  id: String = UUID.randomUUID().toString(),
  ownerId: String = "0",
): Library = Library(
  name = name,
  description = description,
  id = id,
  ownerId = ownerId,
)
