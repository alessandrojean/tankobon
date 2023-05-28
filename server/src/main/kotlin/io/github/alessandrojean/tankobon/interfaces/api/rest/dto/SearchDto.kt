package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

data class SearchDto(
  val books: List<BookEntityDto>,
  val publishers: List<PublisherEntityDto> = emptyList(),
  val series: List<SeriesEntityDto> = emptyList(),
  val stores: List<StoreEntityDto> = emptyList(),
  val people: List<PersonEntityDto> = emptyList(),
  val collections: List<CollectionEntityDto> = emptyList(),
  val tags: List<TagEntityDto> = emptyList(),
)
