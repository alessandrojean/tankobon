package io.github.alessandrojean.tankobon.infrastructure.web

import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToImporterSourceSetConverter : Converter<String, Set<ImporterSource>> {
  override fun convert(source: String): Set<ImporterSource> {
    val types = ImporterSource.values()
    val sources = source.split(",")

    return types.filter { it.name.lowercase() in sources }.toSet()
  }
}
