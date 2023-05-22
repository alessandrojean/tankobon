package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.Dimensions
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.ReadProgressRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import org.javamoney.moneta.FastMoney
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ReadProgressControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
  @Autowired private val collectionRepository: CollectionRepository,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val readProgressRepository: ReadProgressRepository,
) {

  companion object {
    private const val OWNER_ID = "f779266a-d8e8-4e02-9886-0a125dc5ac1d"
    private const val LIBRARY_ID = "a9fe7a00-d573-4468-b117-c81cf02901cc"
    private const val COLLECTION_ID = "e7ee825c-9560-43f8-838c-4714b82460be"
    private const val BOOK_ID = "5b091fd2-693b-4f79-bb4d-c712b9efd6f3"
  }

  private val route = "/api/v1/read-progresses"

  private val owner = TankobonUser("user@example.org", "", false, "User", id = OWNER_ID)
  private val library = makeLibrary("Library", "", id = LIBRARY_ID, ownerId = OWNER_ID)
  private val collection = Collection("Collection", id = COLLECTION_ID, libraryId = LIBRARY_ID)
  private val book = Book(
    id = BOOK_ID,
    code = "12345",
    title = "Book",
    subtitle = "Subtitle",
    paidPrice = FastMoney.of(10.99f, "USD"),
    labelPrice = FastMoney.of(9.99f, "USD"),
    dimensions = Dimensions(widthCm = 13.2f, heightCm = 20f),
    collectionId = collection.id,
    barcode = "12345",
  )

  @BeforeAll
  fun setup() {
    userRepository.insert(owner)
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
  fun deleteBooks() {
    readProgressRepository.deleteAll()
  }

  @Nested
  inner class Creation {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return ok when creating if all attributes are valid`() {
      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "page": 200,
              "startedAt": "2023-03-08T17:52:59.414Z",
              "finishedAt": "2023-03-10T17:52:59.414Z",
              "isCompleted": true,
              "book": "$BOOK_ID"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.response") { value("ENTITY") }
          jsonPath("$.data.type") { value("READ_PROGRESS") }
          jsonPath("$.data.attributes.page") { value(200) }
        }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request if the user tries to create a book with a bad date range`() {
      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "page": 200,
              "startedAt": "2023-03-08T17:52:59.414Z",
              "finishedAt": "2023-03-01T17:52:59.414Z",
              "isCompleted": true,
              "book": "$BOOK_ID"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("VIOLATION_FIELD_ERROR") }
          jsonPath("$.errors[0].title") { value("startedAt") }
        }
    }
  }
}
