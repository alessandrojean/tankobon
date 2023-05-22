package io.github.alessandrojean.tankobon.domain.model

/**
 * Type used to determine the series original name.
 *
 * New types MUST be added at the end of the enum list,
 * as the order is used to save and retrieve from the database.
 */
enum class SeriesType {
  MANGA,
  MANHWA,
  MANHUA,
  COMIC,
  BOOK,
  NOVEL,
  DATABOOK,
  ARTBOOK,
  LIGHT_NOVEL,
}
