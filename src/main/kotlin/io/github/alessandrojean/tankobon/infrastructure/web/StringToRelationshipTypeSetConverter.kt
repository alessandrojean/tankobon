package io.github.alessandrojean.tankobon.infrastructure.web

import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToRelationshipTypeSetConverter : Converter<String, Set<RelationshipType>> {
  override fun convert(source: String): Set<RelationshipType> {
    val types = RelationshipType.values()
    val includes = source.split(",")

    return types.filter { it.name.lowercase() in includes }.toSet()
  }
}
