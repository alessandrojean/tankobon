package io.github.alessandrojean.tankobon.infrastructure.search

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookContributorAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.LibraryAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.PublisherAttributesDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.RelationshipType
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.TagAttributesDto
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField

enum class LuceneEntity(val type: String, val id: String, val defaultFields: Array<String>) {
  Book("book", "book_id", arrayOf("title", "code")),
  Series("series", "series_id", arrayOf("name")),
  Collection("collection", "collection_id", arrayOf("name")),
  Publisher("publisher", "publisher_id", arrayOf("name")),
  Person("person", "person_id", arrayOf("name")),
  ContributorRole("contributor_role", "contributor_role_id", arrayOf("name")),
  Tag("tag", "tag_id", arrayOf("name")),
  Store("store", "store_id", arrayOf("name"));

  companion object {
    const val TYPE = "type"
  }
}

fun BookEntityDto.toDocument() = Document().apply {
  add(TextField("title", attributes.title, Field.Store.NO))
  add(TextField("code", attributes.code.code, Field.Store.NO))
  relationships.orEmpty()
    .filter { it.type == RelationshipType.TAG && it.attributes != null }
    .forEach { add(TextField("tag", (it.attributes as TagAttributesDto).name, Field.Store.NO)) }
  relationships.orEmpty()
    .filter { it.type == RelationshipType.PUBLISHER && it.attributes != null }
    .forEach { add(TextField("publisher", (it.attributes as PublisherAttributesDto).name, Field.Store.NO)) }
  relationships.orEmpty()
    .firstOrNull { it.type == RelationshipType.LIBRARY && it.attributes != null }
    ?.let { add(TextField("library", (it.attributes as LibraryAttributesDto).name, Field.Store.NO)) }
  relationships.orEmpty()
    .filter { it.type == RelationshipType.CONTRIBUTOR && it.attributes != null }
    .forEach {
      val attributes = it.attributes as BookContributorAttributesDto
      add(TextField("contributor", attributes.person.name, Field.Store.NO))
      add(TextField(attributes.role.name, attributes.person.name, Field.Store.NO))
    }
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Book.type, Field.Store.NO))
  add(StringField(LuceneEntity.Book.id, id, Field.Store.YES))
}

fun Series.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Series.type, Field.Store.NO))
  add(StringField(LuceneEntity.Series.id, id, Field.Store.YES))
}

fun Collection.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Collection.type, Field.Store.NO))
  add(StringField(LuceneEntity.Collection.id, id, Field.Store.YES))
}

fun Publisher.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Publisher.type, Field.Store.NO))
  add(StringField(LuceneEntity.Publisher.id, id, Field.Store.YES))
}

fun Person.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Person.type, Field.Store.NO))
  add(StringField(LuceneEntity.Person.id, id, Field.Store.YES))
}

fun ContributorRole.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.ContributorRole.type, Field.Store.NO))
  add(StringField(LuceneEntity.ContributorRole.id, id, Field.Store.YES))
}

fun Tag.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Tag.type, Field.Store.NO))
  add(StringField(LuceneEntity.Tag.id, id, Field.Store.YES))
}

fun Store.toDocument() = Document().apply {
  add(TextField("name", name, Field.Store.NO))
  add(StringField(LuceneEntity.TYPE, LuceneEntity.Store.type, Field.Store.NO))
  add(StringField(LuceneEntity.Store.id, id, Field.Store.YES))
}