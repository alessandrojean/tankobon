package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Image
import io.github.alessandrojean.tankobon.domain.persistence.ImageRepository
import io.github.alessandrojean.tankobon.jooq.tables.records.ImageRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.IMAGE as TableImage

@Component
class ImageDao(private val dsl: DSLContext) : ImageRepository {

  override fun findById(imageId: String): Image =
    dsl.selectFrom(TableImage)
      .where(TableImage.ID.eq(imageId))
      .fetchOne()!!
      .toDomain()

  override fun findByIdOrNull(imageId: String): Image? =
    dsl.selectFrom(TableImage)
      .where(TableImage.ID.eq(imageId))
      .fetchOne()
      ?.toDomain()

  override fun findAllByIds(imageIds: Collection<String>): Collection<Image> =
    dsl.selectFrom(TableImage)
      .where(TableImage.ID.`in`(imageIds))
      .orderBy(TableImage.ID.sortByValues(imageIds.toList(), true))
      .fetchInto(TableImage)
      .map { it.toDomain() }

  @Transactional
  override fun insert(image: Image) {
    dsl.insertInto(TableImage)
      .set(TableImage.ID, image.id)
      .set(TableImage.FILE_NAME, image.fileName)
      .set(TableImage.WIDTH, image.width)
      .set(TableImage.HEIGHT, image.height)
      .set(TableImage.ASPECT_RATIO, image.aspectRatio)
      .set(TableImage.FORMAT, image.format)
      .set(TableImage.MIME_TYPE, image.mimeType)
      .set(TableImage.TIME_HEX, image.timeHex)
      .set(TableImage.BLUR_HASH, image.blurHash)
      .execute()
  }

  @Transactional
  override fun update(image: Image) {
    dsl.update(TableImage)
      .set(TableImage.FILE_NAME, image.fileName)
      .set(TableImage.WIDTH, image.width)
      .set(TableImage.HEIGHT, image.height)
      .set(TableImage.ASPECT_RATIO, image.aspectRatio)
      .set(TableImage.FORMAT, image.format)
      .set(TableImage.MIME_TYPE, image.mimeType)
      .set(TableImage.TIME_HEX, image.timeHex)
      .set(TableImage.BLUR_HASH, image.blurHash)
      .set(TableImage.MODIFIED_AT, LocalDateTime.now(ZoneId.of("Z")))
      .where(TableImage.ID.eq(image.id))
      .execute()
  }

  @Transactional
  override fun delete(imageId: String) {
    dsl.deleteFrom(TableImage)
      .where(TableImage.ID.eq(imageId))
      .execute()
  }

  @Transactional
  override fun deleteAll() {
    dsl.deleteFrom(TableImage).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableImage).toLong()

  override fun countByFolderName(folderName: String): Long =
    dsl.fetchCount(TableImage, TableImage.FILE_NAME.startsWith("$folderName/")).toLong()

  private fun ImageRecord.toDomain(): Image = Image(
    id = id,
    fileName = fileName,
    width = width,
    height = height,
    aspectRatio = aspectRatio,
    format = format,
    mimeType = mimeType,
    timeHex = timeHex,
    blurHash = blurHash,
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )

}