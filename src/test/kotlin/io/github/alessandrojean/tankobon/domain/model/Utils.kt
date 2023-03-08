package io.github.alessandrojean.tankobon.domain.model

import java.util.UUID

fun makeUuid(): String = UUID.randomUUID().toString()

fun makeLibrary(
  name: String = "default",
  description: String = "Default",
  id: String = makeUuid(),
  ownerId: String = "0",
): Library = Library(
  name = name,
  description = description,
  id = id,
  ownerId = ownerId,
)

fun makeBook(
  code: String = makeUuid(),
  title: String = "default",
  paidPrice: MonetaryValue = MonetaryValue(currency = "USD", value = 10.99f),
  labelPrice: MonetaryValue = MonetaryValue(currency = "USD", value = 9.99f),
  dimensions: Dimensions = Dimensions(widthCm = 13.2f, heightCm = 20f),
  id: String = makeUuid(),
  collectionId: String = "0",
): Book = Book(
  code = code,
  title = title,
  paidPrice = paidPrice,
  labelPrice = labelPrice,
  dimensions = dimensions,
  id = id,
  collectionId = collectionId,
)
