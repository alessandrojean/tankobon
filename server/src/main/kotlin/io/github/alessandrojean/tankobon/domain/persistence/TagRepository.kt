package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TagSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TagRepository {
  fun findById(tagId: String): Tag
  fun findByIdOrNull(tagId: String): Tag?
  fun findByLibraryId(libraryId: String): Collection<Tag>

  fun findAll(): Collection<Tag>
  fun findAll(search: TagSearch, pageable: Pageable): Page<Tag>
  fun findAllByIds(tagIds: Collection<String>): Collection<Tag>

  fun existsByNameInLibrary(name: String, libraryId: String): Boolean

  fun getLibraryIdOrNull(tagId: String): String?

  fun insert(tag: Tag)
  fun update(tag: Tag)

  fun delete(tagId: String)
  fun deleteAll()

  fun count(): Long
}
