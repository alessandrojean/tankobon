package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.model.ReadProgress
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
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
class ReadProgressDaoTest(
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val collectionRepository: CollectionRepository,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val readProgressDao: ReadProgressDao,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val user = TankobonUser("user@example.org", "", false, "User", id = "1")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")
  private val collection = Collection(name = "Collection", id = "1", libraryId = "1")
  private val book = Book(
    code = "12345",
    title = "Book",
    subtitle = "Subtitle",
    paidPrice = FastMoney.of(10.99f, "USD"),
    labelPrice = FastMoney.of(9.99f, "USD"),
    dimensions = Dimensions(widthCm = 13.2f, heightCm = 20f),
    id = "1",
    collectionId = collection.id,
    barcode = "12345",
  )

  @BeforeAll
  fun setup() {
    userRepository.insert(user)
    libraryRepository.insert(library)
    collectionRepository.insert(collection)
    bookRepository.insert(book)
  }

  @AfterAll
  fun tearDown() {
    bookRepository.deleteAll()
    collectionRepository.deleteAll()
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun deleteReadProgresses() {
    readProgressDao.deleteAll()
    assertThat(readProgressDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val readProgress = ReadProgress(
      page = 150,
      startedAt = now.toUtcTimeZone(),
      finishedAt = now.toUtcTimeZone(),
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)
    val created = readProgressDao.findById(readProgress.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(page).isEqualTo(readProgress.page)
      assertThat(startedAt).isEqualToIgnoringNanos(now)
      assertThat(finishedAt).isEqualToIgnoringNanos(now)
      assertThat(isCompleted).isEqualTo(readProgress.isCompleted)
      assertThat(bookId).isEqualTo(readProgress.bookId)
      assertThat(userId).isEqualTo(readProgress.userId)
    }
  }

  @Test
  fun `it should persist when updating an existing read progress`() {
    val now = LocalDateTime.now()
    val readProgress = ReadProgress(
      page = 150,
      startedAt = now.toUtcTimeZone(),
      bookId = book.id,
      userId = user.id,
    )
    readProgressDao.insert(readProgress)

    val modificationDate = LocalDateTime.now()

    val updated = readProgressDao.findById(readProgress.id).copy(
      page = 300,
      finishedAt = modificationDate.toUtcTimeZone(),
      isCompleted = true,
    )

    readProgressDao.update(updated)
    val modified = readProgressDao.findById(updated.id)

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
      assertThat(page).isEqualTo(updated.page)
      assertThat(finishedAt).isEqualToIgnoringNanos(modificationDate)
      assertThat(isCompleted).isEqualTo(updated.isCompleted)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val readProgress = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)
    assertThat(readProgressDao.count()).isEqualTo(1)

    readProgressDao.delete(readProgress.id)
    assertThat(readProgressDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all read progresses`() {
    val readProgress = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )
    val readProgress2 = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)
    readProgressDao.insert(readProgress2)
    assertThat(readProgressDao.count()).isEqualTo(2)

    readProgressDao.deleteAll()
    assertThat(readProgressDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all read progresses`() {
    val readProgress = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )
    val readProgress2 = ReadProgress(
      page = 200,
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)
    readProgressDao.insert(readProgress2)

    val all = readProgressDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(ReadProgress::page))
      .containsExactlyInAnyOrder(150, 200)
  }

  @Test
  fun `it should find and return all read progresses by its id's`() {
    val readProgress = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )
    val readProgress2 = ReadProgress(
      page = 200,
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)
    readProgressDao.insert(readProgress2)

    val all = readProgressDao.findAllByIds(listOf(readProgress.id, readProgress2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(ReadProgress::page))
      .containsExactlyInAnyOrder(150, 200)
  }

  @Test
  fun `it should find by id and return a read progress`() {
    val readProgress = ReadProgress(
      page = 150,
      bookId = book.id,
      userId = user.id,
    )

    readProgressDao.insert(readProgress)

    val found = readProgressDao.findByIdOrNull(readProgress.id)
    assertThat(found).isNotNull
    assertThat(found?.page).isEqualTo(150)
  }

  @Test
  fun `it should return null when finding a non-existent read progress`() {
    val found = readProgressDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }
}
