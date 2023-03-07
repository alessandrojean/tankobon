package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.Store
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.StoreRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.StoreLifecycle
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
class StoreControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val storeRepository: StoreRepository,
  @Autowired private val storeLifecycle: StoreLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val owner = TankobonUser("user@example.org", "", true, id = "1")
  private val user = TankobonUser("user2@example.org", "", false, id = "2")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")
  private val store = Store("Store", id = "1", libraryId = "1")

  @BeforeAll
  fun setup() {
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
    storeRepository.deleteAll()
  }

  @Nested
  inner class UnauthorizedUser {
    @Test
    fun `it should return unauthorized when getting the stores from a library with an anonymous user`() {
      mockMvc.get("/api/v1/libraries/${library.id}/stores")
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithMockCustomUser(id = "2")
    fun `it should return forbidden when getting the stores from a library the user does not have access`() {
      mockMvc.get("/api/v1/libraries/${library.id}/stores")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return ok when getting the stores from a library if the user is an admin`() {
      storeLifecycle.addStore(store)

      mockMvc.get("/api/v1/libraries/${library.id}/stores")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(store.id) }
        }
    }
  }

  @Nested
  inner class DuplicateNames {
    @Test
    @WithMockCustomUser(id = "1")
    fun `it should return bad request when creating a store with a duplicate name in the library`() {
      storeLifecycle.addStore(store)

      val jsonString = """
        {
          "name": "${store.name.lowercase()}",
          "description": "",
          "library": "${library.id}"
        }
      """.trimIndent()

      mockMvc
        .post("/api/v1/stores") {
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
    fun `it should return forbidden if a non-admin user tries to delete a store from a library it does not have access`() {
      storeLifecycle.addStore(store)

      mockMvc.delete("/api/v1/stores/${store.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a store from any user`() {
      storeLifecycle.addStore(store)

      mockMvc.delete("/api/v1/stores/${store.id}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/stores/${store.id}")
        .andExpect { status { isNotFound() } }
    }

  }
}