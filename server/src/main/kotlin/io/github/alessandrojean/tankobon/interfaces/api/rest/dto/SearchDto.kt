package io.github.alessandrojean.tankobon.interfaces.api.rest.dto

data class SearchDto(
  val publishers: List<PublisherEntityDto> = emptyList(),
  val series: List<SeriesEntityDto> = emptyList(),
  val stores: List<StoreEntityDto> = emptyList(),
  val people: List<PersonEntityDto> = emptyList(),
)
