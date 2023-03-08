package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class LibraryControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val ADMIN_ID = "a327ed61-f39f-44ce-bf01-1438e4a0f0f0"
    private const val OWNER_ID = "f779266a-d8e8-4e02-9886-0a125dc5ac1d"
    private const val LIBRARY_ID = "a9fe7a00-d573-4468-b117-c81cf02901cc"
  }

  private val route = "/api/v1/libraries"

  private val admin = TankobonUser("admin@example.org", "", true, id = ADMIN_ID)
  private val owner = TankobonUser("user@example.org", "", false, id = OWNER_ID)
  private val library = makeLibrary("Library", "", id = LIBRARY_ID, ownerId = OWNER_ID)

  @BeforeAll
  fun setup() {
    userRepository.insert(owner)
    libraryRepository.insert(library)
  }

  @AfterAll
  fun tearDown() {
    libraryRepository.deleteAll()
    userRepository.deleteAll()
  }

  @Nested
  inner class AnonymousUser {
    @Test
    @WithAnonymousUser
    fun `it should return unauthorized with an anonymous user`() {
      mockMvc.get(route)
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithAnonymousUser
    fun `it should return unauthorized when creating with an anonymous user`() {
      val jsonString = """{"name":"test", description:"test"}"""

      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }
        .andExpect { status { isUnauthorized() } }
    }
  }

  @Nested
  inner class UserRole {
    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should return ok if the user has access to all libraries`() {
      mockMvc.get(route)
        .andExpect { status { isOk() } }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should only return the libraries the user has access`() {
      mockMvc.get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(LIBRARY_ID) }
        }
    }

    @Test
    @WithMockCustomUser
    fun `it should return forbidden when the user does not have access to the library`() {
      mockMvc.get("$route/${library.id}")
        .andExpect {
          status { isForbidden() }
        }
    }
  }
}