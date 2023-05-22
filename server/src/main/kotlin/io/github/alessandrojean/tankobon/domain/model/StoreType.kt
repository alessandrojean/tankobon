package io.github.alessandrojean.tankobon.domain.model

/**
 * Type used to determine the store type.
 *
 * New types MUST be added at the end of the enum list,
 * as the order is used to save and retrieve from the database.
 */
enum class StoreType {
  COMIC_SHOP,
  BOOKSTORE,
  NEWSSTAND,
}
