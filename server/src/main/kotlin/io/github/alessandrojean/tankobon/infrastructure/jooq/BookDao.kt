package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.infrastructure.importer.ImporterSource
import io.github.alessandrojean.tankobon.jooq.tables.records.BookRecord
import org.javamoney.moneta.FastMoney
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import io.github.alessandrojean.tankobon.jooq.Tables.BOOK as TableBook
import io.github.alessandrojean.tankobon.jooq.Tables.COLLECTION as TableCollection

@Component
class BookDao(
  private val dsl: DSLContext,
) : BookRepository {

  override fun findByIdOrNull(bookId: String): Book? =
    dsl.selectFrom(TableBook)
      .where(TableBook.ID.eq(bookId))
      .fetchOneInto(TableBook)
      ?.toDomain()

  override fun findByCodeOrNull(bookCode: String): Book? =
    dsl.selectFrom(TableBook)
      .where(TableBook.CODE.eq(bookCode))
      .fetchOneInto(TableBook)
      ?.toDomain()

  override fun findAll(): Collection<Book> =
    dsl.selectFrom(TableBook)
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllByIds(bookIds: Collection<String>): Collection<Book> =
    dsl.selectFrom(TableBook)
      .where(TableBook.ID.`in`(bookIds))
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllBySeriesId(seriesId: String): Collection<Book> =
    dsl.selectFrom(TableBook)
      .where(TableBook.SERIES_ID.eq(seriesId))
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllByStoreId(storeId: String): Collection<Book> =
    dsl.selectFrom(TableBook)
      .where(TableBook.STORE_ID.eq(storeId))
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllByLibraryId(libraryId: String): Collection<Book> =
    dsl.select(*TableBook.fields())
      .from(TableBook)
      .leftJoin(TableCollection)
      .on(TableBook.COLLECTION_ID.eq(TableCollection.ID))
      .where(TableCollection.LIBRARY_ID.eq(libraryId))
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findFirstIdInSeriesOrNull(seriesId: String): String? =
    dsl.select(TableBook.ID)
      .from(TableBook)
      .where(TableBook.SERIES_ID.eq(seriesId))
      .orderBy(TableBook.NUMBER)
      .limit(1)
      .fetchOne(TableBook.ID)

  override fun findLastIdInSeriesOrNull(seriesId: String): String? =
    dsl.select(TableBook.ID)
      .from(TableBook)
      .where(TableBook.SERIES_ID.eq(seriesId))
      .orderBy(TableBook.NUMBER.desc())
      .limit(1)
      .fetchOne(TableBook.ID)

  override fun findAllIdsBySeriesId(seriesId: String): Collection<String> =
    dsl.select(TableBook.ID)
      .from(TableBook)
      .where(TableBook.SERIES_ID.eq(seriesId))
      .fetch(TableBook.ID)

  override fun findAllIdsByLibraryId(libraryId: String): Collection<String> =
    dsl.select(TableBook.ID)
      .from(TableBook)
      .leftJoin(TableCollection)
      .on(TableBook.COLLECTION_ID.eq(TableCollection.ID))
      .where(TableCollection.LIBRARY_ID.eq(libraryId))
      .fetch(TableBook.ID)

  override fun existsByCode(code: String): Boolean =
    dsl.fetchExists(
      dsl.select(TableBook.ID)
        .from(TableBook)
        .where(TableBook.CODE.equalIgnoreCase(code))
    )

  override fun getLibraryIdOrNull(bookId: String): String? =
    dsl.select(TableCollection.LIBRARY_ID)
      .from(TableCollection)
      .leftJoin(TableBook)
      .on(TableBook.COLLECTION_ID.eq(TableCollection.ID))
      .where(TableBook.ID.eq(bookId))
      .fetchOne(TableCollection.LIBRARY_ID)

  @Transactional
  override fun insert(book: Book) {
    dsl.insertInto(TableBook)
      .set(TableBook.ID, book.id)
      .set(TableBook.CODE, book.code)
      .set(TableBook.BARCODE, book.barcode)
      .set(TableBook.COLLECTION_ID, book.collectionId)
      .set(TableBook.STORE_ID, book.storeId)
      .set(TableBook.SERIES_ID, book.seriesId)
      .set(TableBook.TITLE, book.title)
      .set(TableBook.IS_IN_LIBRARY, book.isInLibrary)
      .set(TableBook.PAID_PRICE_CURRENCY, book.paidPrice.currency.currencyCode)
      .set(TableBook.PAID_PRICE_VALUE, book.paidPrice.number.toFloat())
      .set(TableBook.LABEL_PRICE_CURRENCY, book.labelPrice.currency.currencyCode)
      .set(TableBook.LABEL_PRICE_VALUE, book.labelPrice.number.toFloat())
      .set(TableBook.DIMENSION_WIDTH_CM, book.dimensions.widthCm)
      .set(TableBook.DIMENSION_HEIGHT_CM, book.dimensions.heightCm)
      .set(TableBook.NUMBER, book.number)
      .set(TableBook.PAGE_COUNT, book.pageCount)
      .set(TableBook.SOURCE_KEY, book.source?.ordinal)
      .set(TableBook.SOURCE_BOOK_ID, book.sourceBookId)
      .set(TableBook.BOUGHT_AT, book.boughtAt)
      .set(TableBook.BILLED_AT, book.billedAt)
      .set(TableBook.ARRIVED_AT, book.arrivedAt)
      .set(TableBook.SYNOPSIS, book.synopsis)
      .set(TableBook.NOTES, book.notes)
      .set(TableBook.SUBTITLE, book.subtitle)
      .execute()
  }

  @Transactional
  override fun update(book: Book) {
    dsl.update(TableBook)
      .set(TableBook.CODE, book.code)
      .set(TableBook.BARCODE, book.barcode)
      .set(TableBook.COLLECTION_ID, book.collectionId)
      .set(TableBook.STORE_ID, book.storeId)
      .set(TableBook.SERIES_ID, book.seriesId)
      .set(TableBook.TITLE, book.title)
      .set(TableBook.IS_IN_LIBRARY, book.isInLibrary)
      .set(TableBook.PAID_PRICE_CURRENCY, book.paidPrice.currency.currencyCode)
      .set(TableBook.PAID_PRICE_VALUE, book.paidPrice.number.toFloat())
      .set(TableBook.LABEL_PRICE_CURRENCY, book.labelPrice.currency.currencyCode)
      .set(TableBook.LABEL_PRICE_VALUE, book.labelPrice.number.toFloat())
      .set(TableBook.DIMENSION_WIDTH_CM, book.dimensions.widthCm)
      .set(TableBook.DIMENSION_HEIGHT_CM, book.dimensions.heightCm)
      .set(TableBook.NUMBER, book.number)
      .set(TableBook.PAGE_COUNT, book.pageCount)
      .set(TableBook.SOURCE_KEY, book.source?.ordinal)
      .set(TableBook.SOURCE_BOOK_ID, book.sourceBookId)
      .set(TableBook.BOUGHT_AT, book.boughtAt)
      .set(TableBook.BILLED_AT, book.billedAt)
      .set(TableBook.ARRIVED_AT, book.arrivedAt)
      .set(TableBook.SYNOPSIS, book.synopsis)
      .set(TableBook.NOTES, book.notes)
      .set(TableBook.SUBTITLE, book.subtitle)
      .set(TableBook.MODIFIED_AT, LocalDateTime.now(ZoneId.of("UTC")))
      .where(TableBook.ID.eq(book.id))
      .execute()
  }

  override fun delete(bookId: String) {
    dsl.deleteFrom(TableBook).where(TableBook.ID.eq(bookId)).execute()
  }

  override fun deleteAll() {
    dsl.deleteFrom(TableBook).execute()
  }

  override fun count(): Long = dsl.fetchCount(TableBook).toLong()

  private fun BookRecord.toDomain() = Book(
    code = code,
    title = title,
    paidPrice = FastMoney.of(paidPriceValue, paidPriceCurrency),
    labelPrice = FastMoney.of(labelPriceValue, labelPriceCurrency),
    dimensions = Dimensions(
      widthCm = dimensionWidthCm,
      heightCm = dimensionHeightCm,
    ),
    id = id,
    collectionId = collectionId,
    storeId = storeId,
    seriesId = seriesId,
    barcode = barcode,
    isInLibrary = isInLibrary,
    number = number,
    pageCount = pageCount,
    synopsis = synopsis,
    notes = notes,
    source = sourceKey?.let { ImporterSource.values().getOrNull(it) },
    sourceBookId = sourceBookId,
    subtitle = subtitle,
    boughtAt = boughtAt?.toCurrentTimeZone(),
    billedAt = billedAt?.toCurrentTimeZone(),
    arrivedAt = arrivedAt?.toCurrentTimeZone(),
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )
}