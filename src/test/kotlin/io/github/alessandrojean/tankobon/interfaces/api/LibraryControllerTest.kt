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

  private val route = "/api/v1/libraries"

  private val owner = TankobonUser("user@example.org", "", false, id = "1")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")

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
    @WithMockCustomUser(id = "0")
    fun `it should return ok if the user has access to all libraries`() {
      mockMvc.get(route)
        .andExpect { status { isOk() } }
    }
  }

  @Nested
  inner class LimitedUser {
    @Test
    @WithMockCustomUser(id = "1")
    fun `it should only return the libraries the user has access`() {
      mockMvc.get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$[0].id") { value("1") }
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