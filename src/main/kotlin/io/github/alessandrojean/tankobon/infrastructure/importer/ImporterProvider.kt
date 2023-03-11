package io.github.alessandrojean.tankobon.infrastructure.importer

abstract class ImporterProvider {

  protected abstract val baseUrl: String

  protected abstract val key: ImporterSource

  abstract fun searchByIsbn(isbn: String): Collection<ImporterBookResult>

}
