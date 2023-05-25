package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.BookLinks
import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.model.LengthUnit
import io.github.alessandrojean.tankobon.domain.model.MassUnit
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.Weight
import io.github.alessandrojean.tankobon.domain.model.makeBook
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.javamoney.moneta.FastMoney
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class BookDaoTest(
  @Autowired private val bookDao: BookDao,
  @Autowired private val collectionRepository: CollectionRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val owner = TankobonUser("user@example.org", "", false, "User", "1")
  private val library = makeLibrary(id = "1", ownerId = "1")
  private val collection = Collection(name = "Collection", libraryId = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(owner)
    libraryRepository.insert(library)
    collectionRepository.insert(collection)
  }

  @AfterEach
  fun deleteBooks() {
    bookDao.deleteAll()
    assertThat(bookDao.count()).isEqualTo(0)
  }

  @AfterAll
  fun tearDown() {
    collectionRepository.deleteAll()
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @Test
  fun `it should persist the book when inserting`() {
    val now = LocalDateTime.now()
    val book = Book(
      code = "12345",
      title = "Book",
      subtitle = "Subtitle",
      paidPrice = FastMoney.of(10.99f, "USD"),
      labelPrice = FastMoney.of(9.99f, "USD"),
      dimensions = Dimensions(
        width = 13.2f,
        height = 20f,
        depth = 1.5f,
        unit = LengthUnit.CENTIMETER,
      ),
      weight = Weight(
        value = 0.2f,
        unit = MassUnit.KILOGRAM,
      ),
      links = BookLinks(amazon = "amazon.com"),
      collectionId = collection.id,
      barcode = "12345",
      boughtAt = now.toUtcTimeZone(),
      billedAt = now.toUtcTimeZone(),
      arrivedAt = now.toUtcTimeZone(),
    )

    bookDao.insert(book)
    val created = bookDao.findByIdOrNull(book.id)!!

    with(created) {
      assertThat(id).isEqualTo(book.id)
      assertThat(code).isEqualTo(book.code)
      assertThat(paidPrice).isEqualTo(book.paidPrice)
      assertThat(labelPrice).isEqualTo(book.labelPrice)
      assertThat(dimensions.width).isEqualTo(book.dimensions.width)
      assertThat(dimensions.height).isEqualTo(book.dimensions.height)
      assertThat(dimensions.depth).isEqualTo(book.dimensions.depth)
      assertThat(dimensions.unit).isEqualTo(book.dimensions.unit)
      assertThat(weight.value).isEqualTo(book.weight.value)
      assertThat(weight.unit).isEqualTo(book.weight.unit)
      assertThat(collectionId).isEqualTo(book.collectionId)
      assertThat(barcode).isEqualTo(book.barcode)
      assertThat(boughtAt).isEqualToIgnoringNanos(now)
      assertThat(billedAt).isEqualToIgnoringNanos(now)
      assertThat(arrivedAt).isEqualToIgnoringNanos(now)
      assertThat(title).isEqualTo(book.title)
      assertThat(subtitle).isEqualTo(book.subtitle)
      assertThat(links.amazon).isEqualTo(book.links.amazon)
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
    }
  }

  @Test
  fun `it should persist the book when updating`() {
    val now = LocalDateTime.now()
    val book = Book(
      code = "12345",
      title = "Book",
      subtitle = "Subtitle",
      paidPrice = FastMoney.of(10.99f, "USD"),
      labelPrice = FastMoney.of(9.99f, "USD"),
      dimensions = Dimensions(
        width = 13.2f,
        height = 20f,
        depth = 1.5f,
        unit = LengthUnit.CENTIMETER,
      ),
      weight = Weight(
        value = 0.2f,
        unit = MassUnit.KILOGRAM,
      ),
      collectionId = collection.id,
      barcode = "12345",
      links = BookLinks(amazon = "amazon.com"),
      boughtAt = now.toUtcTimeZone(),
      billedAt = now.toUtcTimeZone(),
      arrivedAt = now.toUtcTimeZone(),
    )
    bookDao.insert(book)

    val modificationDate = LocalDateTime.now()

    val updated = with(bookDao.findByIdOrNull(book.id)!!) {
      copy(
        title = "Updated",
        subtitle = "Subtitle updated",
        weight = weight.copy(value = 0.3f),
        boughtAt = modificationDate.toUtcTimeZone(),
        billedAt = modificationDate.toUtcTimeZone(),
        arrivedAt = modificationDate.toUtcTimeZone(),
        paidPrice = FastMoney.of(5.99f, "USD"),
        links = BookLinks(amazon = "amazon.com.br"),
      )
    }

    bookDao.update(updated)
    val modified = bookDao.findByIdOrNull(updated.id)!!

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(code).isEqualTo(updated.code)
      assertThat(paidPrice).isEqualTo(updated.paidPrice)
      assertThat(collectionId).isEqualTo(updated.collectionId)
      assertThat(barcode).isEqualTo(updated.barcode)
      assertThat(boughtAt).isEqualToIgnoringNanos(modificationDate)
      assertThat(billedAt).isEqualToIgnoringNanos(modificationDate)
      assertThat(arrivedAt).isEqualToIgnoringNanos(modificationDate)
      assertThat(title).isEqualTo(updated.title)
      assertThat(subtitle).isEqualTo(updated.subtitle)
      assertThat(weight.value).isEqualTo(updated.weight.value)
      assertThat(links.amazon).isEqualTo(updated.links.amazon)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
    }
  }

  @Test
  fun `it should return an existing book when finding by id`() {
    val book = Book(
      code = "12345",
      title = "Book",
      subtitle = "Subtitle",
      paidPrice = FastMoney.of(10.99f, "USD"),
      labelPrice = FastMoney.of(9.99f, "USD"),
      dimensions = Dimensions(
        width = 13.2f,
        height = 20f,
        depth = 1.5f,
        unit = LengthUnit.CENTIMETER,
      ),
      collectionId = collection.id,
      barcode = "12345",
    )
    bookDao.insert(book)

    val found = bookDao.findByIdOrNull(book.id)

    assertThat(found).isNotNull
    assertThat(found?.title).isEqualTo(book.title)
  }

  @Test
  fun `it should return null when finding a non-existent book by id`() {
    val found = bookDao.findByIdOrNull("12987")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return all books when finding all`() {
    bookDao.insert(makeBook(id = "1", collectionId = collection.id))
    bookDao.insert(makeBook(id = "2", collectionId = collection.id))

    val found = bookDao.findAll()
    assertThat(found).hasSize(2)
  }

  @Test
  fun `it should return all book ids when finding all books from a library`() {
    bookDao.insert(makeBook(id = "1", collectionId = collection.id))
    bookDao.insert(makeBook(id = "2", collectionId = collection.id))

    val found = bookDao.findAllIdsByLibraryId(library.id)
    assertThat(found).hasSize(2)
  }

  @Test
  fun `it should have a count equal to zero when deleting all`() {
    bookDao.insert(makeBook(id = "1", collectionId = collection.id))
    bookDao.insert(makeBook(id = "2", collectionId = collection.id))

    bookDao.deleteAll()

    assertThat(bookDao.count()).isEqualTo(0)
  }
}
