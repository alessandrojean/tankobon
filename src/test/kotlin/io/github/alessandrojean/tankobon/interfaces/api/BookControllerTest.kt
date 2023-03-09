package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.persistence.SeriesRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
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
class BookControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
  @Autowired private val collectionRepository: CollectionRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val storeRepository: StoreRepository,
  @Autowired private val personRepository: PersonRepository,
  @Autowired private val contributorRoleRepository: ContributorRoleRepository,
  @Autowired private val publisherRepository: PublisherRepository,
  @Autowired private val tagRepository: TagRepository,
  @Autowired private val bookRepository: BookRepository,
) {

  companion object {
    private const val OWNER_ID = "f779266a-d8e8-4e02-9886-0a125dc5ac1d"
    private const val LIBRARY_ID = "a9fe7a00-d573-4468-b117-c81cf02901cc"
    private const val COLLECTION_ID = "e7ee825c-9560-43f8-838c-4714b82460be"
    private const val SERIES_ID = "5b091fd2-693b-4f79-bb4d-c712b9efd6f3"
    private const val STORE_ID = "288172a1-e494-47ba-b472-232eaacec15c"
    private const val PERSON_ID = "288172a1-e494-47ba-b472-232eaacec15c"
    private const val CONTRIBUTOR_ROLE_ID = "146a7033-bb5d-41d9-b965-8e6740353e07"
    private const val PUBLISHER_ID = "e8618236-ea65-43e8-9545-49ab94ffc9d5"
    private const val TAG_ID = "ed0f9cbf-d2d0-42de-84b0-2054e476d9d3"
  }

  private val route = "/api/v1/books"

  private val owner = TankobonUser("user@example.org", "", false, id = OWNER_ID)
  private val library = makeLibrary("Library", "", id = LIBRARY_ID, ownerId = OWNER_ID)
  private val collection = Collection("Collection", id = COLLECTION_ID, libraryId = LIBRARY_ID)
  private val series = Series("Series", id = SERIES_ID, libraryId = LIBRARY_ID)
  private val store = Store("Store", id = STORE_ID, libraryId = LIBRARY_ID)
  private val person = Person("Person", id = PERSON_ID, libraryId = LIBRARY_ID)
  private val contributorRole = ContributorRole("ContributorRole", id = CONTRIBUTOR_ROLE_ID, libraryId = LIBRARY_ID)
  private val publisher = Publisher("Publisher", id = PUBLISHER_ID, libraryId = LIBRARY_ID)
  private val tag = Tag("Tag", id = TAG_ID, libraryId = LIBRARY_ID)

  @BeforeAll
  fun setup() {
    userRepository.insert(owner)
    libraryRepository.insert(library)
    collectionRepository.insert(collection)
    seriesRepository.insert(series)
    storeRepository.insert(store)
    personRepository.insert(person)
    tagRepository.insert(tag)
    contributorRoleRepository.insert(contributorRole)
    publisherRepository.insert(publisher)
  }

  @AfterAll
  fun tearDown() {
    bookRepository.deleteAll()
    collectionRepository.deleteAll()
    seriesRepository.deleteAll()
    storeRepository.deleteAll()
    personRepository.deleteAll()
    contributorRoleRepository.deleteAll()
    publisherRepository.deleteAll()
    tagRepository.deleteAll()
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun deleteBooks() {
    bookRepository.deleteAll()
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
              "arrivedAt": "2023-03-08T17:52:59.414Z",
              "barcode": "9788545702870",
              "billedAt": "2023-03-08T17:52:59.414Z",
              "boughtAt": "2023-03-08T17:52:59.414Z",
              "code": "9788545702870",
              "collection": "$COLLECTION_ID",
              "contributors": [
                {
                  "person": "$PERSON_ID",
                  "role": "$CONTRIBUTOR_ROLE_ID"
                }
              ],
              "dimensions": {
                "heightCm": 25.6,
                "widthCm": 17.8
              },
              "isInLibrary": true,
              "labelPrice": {
                "currency": "BRL",
                "amount": 69.9
              },
              "notes": "",
              "number": "1",
              "pageCount": 300,
              "paidPrice": {
                "currency": "BRL",
                "amount": 69.9
              },
              "publishers": ["$PUBLISHER_ID"],
              "series": "$SERIES_ID",
              "store": "$STORE_ID",
              "synopsis": "Synopsis",
              "tags": ["$TAG_ID"],
              "title": "Akira #01: Part 1 - Tetsuo"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.response") { value("ENTITY") }
          jsonPath("$.data.type") { value("BOOK") }
          jsonPath("$.data.attributes.title") { value("Akira #01: Part 1 - Tetsuo") }
        }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request if the user tries to create a book with items from different libraries`() {
      val library2 = makeLibrary("Library2", "", ownerId = OWNER_ID)
      libraryRepository.insert(library2)

      val otherSeries = Series("Other series", libraryId = library2.id)
      seriesRepository.insert(otherSeries)

      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "arrivedAt": "2023-03-08T17:52:59.414Z",
              "barcode": "9788545702870",
              "billedAt": "2023-03-08T17:52:59.414Z",
              "boughtAt": "2023-03-08T17:52:59.414Z",
              "code": "9788545702870",
              "collection": "$COLLECTION_ID",
              "contributors": [
                {
                  "person": "$PERSON_ID",
                  "role": "$CONTRIBUTOR_ROLE_ID"
                }
              ],
              "dimensions": {
                "heightCm": 25.6,
                "widthCm": 17.8
              },
              "isInLibrary": true,
              "labelPrice": {
                "currency": "BRL",
                "amount": 69.9
              },
              "notes": "",
              "number": "1",
              "pageCount": 300,
              "paidPrice": {
                "currency": "BRL",
                "amount": 69.9
              },
              "publishers": ["$PUBLISHER_ID"],
              "series": "${otherSeries.id}",
              "store": "$STORE_ID",
              "synopsis": "Synopsis",
              "tags": ["$TAG_ID"],
              "title": "Akira #01: Part 1 - Tetsuo"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("RelationIsNotFromSameLibraryException") }
        }
    }
  }

}