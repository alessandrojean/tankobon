package io.github.alessandrojean.tankobon.infrastructure.importer.skoob

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class SkoobResponseDto<T>(
  val success: Boolean = false,
  val error: String? = "",
  val response: T? = null
)

data class SkoobBookDto(
  @field:JsonProperty("livro_id") val bookId: Int,
  @field:JsonProperty("titulo") val title: String? = "",
  @field:JsonProperty("autor") val author: String? = "",
  @set:JsonDeserialize(using = SkoobIsbnDeserializer::class)
  var isbn: String? = "",
  @field:JsonProperty("editora") val publisher: String? = "",
  @field:JsonProperty("sinopse") val synopsis: String? = "",
  @field:JsonProperty("paginas") val pageCount: Int? = 0,
  @field:JsonProperty("capa_grande") val coverUrl: String? = "",
)

class SkoobIsbnDeserializer : JsonDeserializer<String>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
    return when (p.currentToken) {
      JsonToken.VALUE_STRING -> p.text.replace("-", "")
      JsonToken.VALUE_NUMBER_INT -> p.numberValue.toString()
      else -> null
    }
  }
}