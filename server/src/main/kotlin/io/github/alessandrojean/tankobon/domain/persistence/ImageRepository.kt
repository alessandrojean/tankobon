package io.github.alessandrojean.tankobon.domain.persistence

import io.github.alessandrojean.tankobon.domain.model.Image

interface ImageRepository {
  fun findById(imageId: String): Image
  fun findByIdOrNull(imageId: String): Image?

  fun findAllByIds(imageIds: Collection<String>): Collection<Image>

  fun insert(image: Image)
  fun update(image: Image)

  fun delete(imageId: String)
  fun deleteAll()

  fun count(): Long
  fun countByFolderName(folderName: String): Long
}
