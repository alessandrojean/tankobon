package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.BookLinks
import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.model.LengthUnit
import io.github.alessandrojean.tankobon.domain.model.MassUnit
import io.github.alessandrojean.tankobon.domain.model.Weight
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

  override fun findByCodeInLibraryOrNull(bookCode: String, libraryId: String): Book? =
    dsl.select(*TableBook.fields())
      .from(TableBook)
      .leftJoin(TableCollection)
      .on(TableCollection.ID.eq(TableBook.COLLECTION_ID))
      .where(TableBook.CODE.eq(bookCode))
      .and(TableCollection.LIBRARY_ID.eq(libraryId))
      .fetchOneInto(TableBook)
      ?.toDomain()

  override fun findAll(): Collection<Book> =
    dsl.selectFrom(TableBook)
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllByIds(bookIds: Collection<String>): Collection<Book> =
    dsl.selectFrom(TableBook)
      .where(TableBook.ID.`in`(bookIds))
      .orderBy(TableBook.ID.sortByValues(bookIds.toList(), true))
      .fetchInto(TableBook)
      .map { it.toDomain() }

  override fun findAllBySeriesId(seriesId: String): Collection<Book> =
    dsl.selectFrom(TableBook)
      .where(TableBook.SERIES_ID.eq(seriesId))
      .orderBy(TableBook.NUMBER.asc())
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
        .where(TableBook.CODE.equalIgnoreCase(code)),
    )

  override fun existsByCodeInLibrary(code: String, libraryId: String): Boolean =
    dsl.fetchExists(
      dsl.select(TableBook.ID)
        .from(TableBook)
        .leftJoin(TableCollection)
        .on(TableBook.COLLECTION_ID.eq(TableCollection.ID))
        .where(TableBook.CODE.equalIgnoreCase(code))
        .and(TableCollection.LIBRARY_ID.eq(libraryId)),
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
      .set(TableBook.DIMENSION_WIDTH, book.dimensions.width)
      .set(TableBook.DIMENSION_HEIGHT, book.dimensions.height)
      .set(TableBook.DIMENSION_DEPTH, book.dimensions.depth)
      .set(TableBook.DIMENSION_UNIT, book.dimensions.unit.ordinal)
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
      .set(TableBook.WEIGHT, book.weight.value)
      .set(TableBook.WEIGHT_UNIT, book.weight.unit.ordinal)
      .set(TableBook.AMAZON, book.links.amazon)
      .set(TableBook.OPEN_LIBRARY, book.links.openLibrary)
      .set(TableBook.SKOOB, book.links.skoob)
      .set(TableBook.GOODREADS, book.links.goodreads)
      .set(TableBook.GUIA_DOS_QUADRINHOS, book.links.guiaDosQuadrinhos)
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
      .set(TableBook.DIMENSION_WIDTH, book.dimensions.width)
      .set(TableBook.DIMENSION_HEIGHT, book.dimensions.height)
      .set(TableBook.DIMENSION_DEPTH, book.dimensions.depth)
      .set(TableBook.DIMENSION_UNIT, book.dimensions.unit.ordinal)
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
      .set(TableBook.WEIGHT, book.weight.value)
      .set(TableBook.WEIGHT_UNIT, book.weight.unit.ordinal)
      .set(TableBook.AMAZON, book.links.amazon)
      .set(TableBook.OPEN_LIBRARY, book.links.openLibrary)
      .set(TableBook.SKOOB, book.links.skoob)
      .set(TableBook.GOODREADS, book.links.goodreads)
      .set(TableBook.GUIA_DOS_QUADRINHOS, book.links.guiaDosQuadrinhos)
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
      width = dimensionWidth,
      height = dimensionHeight,
      depth = dimensionDepth,
      unit = LengthUnit.values().getOrElse(dimensionUnit) { LengthUnit.CENTIMETER },
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
    weight = Weight(
      value = weight,
      unit = MassUnit.values().getOrElse(weightUnit) { MassUnit.KILOGRAM },
    ),
    links = BookLinks(
      amazon = amazon,
      openLibrary = openLibrary,
      skoob = skoob,
      goodreads = goodreads,
      guiaDosQuadrinhos = guiaDosQuadrinhos,
    ),
    boughtAt = boughtAt?.toCurrentTimeZone(),
    billedAt = billedAt?.toCurrentTimeZone(),
    arrivedAt = arrivedAt?.toCurrentTimeZone(),
    createdAt = createdAt.toCurrentTimeZone(),
    modifiedAt = modifiedAt.toCurrentTimeZone(),
  )
}
