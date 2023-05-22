package io.github.alessandrojean.tankobon.infrastructure.importer.cbl

import com.fasterxml.jackson.annotation.JsonProperty

data class CblSearchRequestDto(
  val count: Boolean,
  val facets: List<String>,
  val filter: String,
  @get:JsonProperty("orderby") val orderBy: String?,
  val queryType: String,
  val search: String,
  val searchFields: String,
  val searchMode: String,
  val select: String,
  val skip: Int,
  val top: Int,
)

data class CblSearchResultDto(
  @field:JsonProperty("@odata.count") val count: Int = 0,
  val value: List<CblRecordDto> = emptyList(),
)

data class CblRecordDto(
  @field:JsonProperty("RecordId") val id: String? = "",
  @field:JsonProperty("Authors") val authors: List<String> = emptyList(),
  @field:JsonProperty("Dimensao") val dimensions: String? = "",
  @field:JsonProperty("Paginas") val pageCount: String? = "",
  @field:JsonProperty("Imprint") val publisher: String = "",
  @field:JsonProperty("Profissoes") val roles: List<String>? = emptyList(),
  @field:JsonProperty("RowKey") val rowKey: String = "",
  @field:JsonProperty("Sinopse") val synopsis: String? = "",
  @field:JsonProperty("Title") val title: String = "",
)
