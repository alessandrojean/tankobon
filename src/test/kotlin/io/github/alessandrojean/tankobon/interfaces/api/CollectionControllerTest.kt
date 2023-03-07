package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.Collection
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.CollectionLifecycle
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class CollectionControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val collectionRepository: CollectionRepository,
  @Autowired private val collectionLifecycle: CollectionLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val admin = TankobonUser("admin@example.org", "", true, id = "0")
  private val owner = TankobonUser("user@example.org", "", true, id = "1")
  private val user = TankobonUser("user2@example.org", "", false, id = "2")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")
  private val collection = Collection("Collection", id = "1", libraryId = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(admin)
    userRepository.insert(owner)
    userRepository.insert(user)
    libraryRepository.insert(library)
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @AfterEach
  fun clear() {
    collectionRepository.deleteAll()
  }

  @Nested
  inner class UnauthorizedUser {
    @Test
    fun `it should return unauthorized when getting the collections from a library with an anonymous user`() {
      mockMvc.get("/api/v1/libraries/${library.id}/collections")
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithMockCustomUser(id = "2")
    fun `it should return forbidden when getting the collections from a library the user does not have access`() {
      mockMvc.get("/api/v1/libraries/${library.id}/collections")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return ok when getting the collections from a library if the user is an admin`() {
      collectionLifecycle.addCollection(collection)

      mockMvc.get("/api/v1/libraries/${library.id}/collections")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(collection.id) }
        }
    }
  }

  @Nested
  inner class DuplicateNames {
    @Test
    @WithMockCustomUser(id = "1")
    fun `it should return bad request when creating a collection with a duplicate name in the library`() {
      collectionLifecycle.addCollection(collection)

      val jsonString = """
        {
          "name": "${collection.name.lowercase()}",
          "description": "",
          "library": "${library.id}"
        }
      """.trimIndent()

      mockMvc
        .post("/api/v1/collections") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }
        .andExpect { status { isBadRequest() } }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser(id = "2")
    fun `it should return forbidden if a non-admin user tries to delete a collection from a library it does not have access`() {
      collectionLifecycle.addCollection(collection)

      mockMvc.delete("/api/v1/collections/${collection.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a collection from any user`() {
      collectionLifecycle.addCollection(collection)

      mockMvc.delete("/api/v1/collections/${collection.id}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/collections/${collection.id}")
        .andExpect { status { isNotFound() } }
    }

  }
}