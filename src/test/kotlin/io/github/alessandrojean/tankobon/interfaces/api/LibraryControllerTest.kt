package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.model.makeUuid
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import org.hamcrest.Matchers.containsInAnyOrder
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
import org.springframework.test.web.servlet.put

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class LibraryControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val OWNER_ID = "f779266a-d8e8-4e02-9886-0a125dc5ac1d"
    private const val LIBRARY_ID = "a9fe7a00-d573-4468-b117-c81cf02901cc"
  }

  private val route = "/api/v1/libraries"

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
        .andExpect {
          status { isUnauthorized() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors[0].id") { value("InsufficientAuthenticationException") }
        }
    }
  }

  @Nested
  inner class UserRole {
    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return ok if the user has access to all libraries`() {
      mockMvc.get(route)
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.response") { value("COLLECTION") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(LIBRARY_ID) }
        }
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
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors[0].id") { value("UserDoesNotHaveAccessException") }
        }
    }
  }

  @Nested
  inner class Creation {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should validate the creation body properly`() {
      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "name": "",
              "description": "",
              "owner": "1234"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(2) }
          jsonPath("$.errors[*].title") {
            value(containsInAnyOrder("owner", "name"))
          }
        }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request if the user tries to create a library for a non-existent user`() {
      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "name": "Library",
              "description": "",
              "owner": "${makeUuid()}"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("RelationIdDoesNotExistException") }
        }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return forbidden if a non-admin user tries to create a library for other user`() {
      val otherUser = TankobonUser("user2@example.org", "", false)
      userRepository.insert(otherUser)

      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "name": "Library",
              "description": "",
              "owner": "${otherUser.id}"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isForbidden() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("UserDoesNotHaveAccessException") }
        }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request if the user tries to create a library with an existing name`() {
      mockMvc
        .post(route) {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "name": "Library",
              "description": ""
            }
          """.trimIndent()
        }
        .andExpect {
          status { isBadRequest() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("DuplicateNameException") }
        }
    }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return forbidden if a non-admin user tries to change a library owner`() {
      mockMvc
        .put("$route/$LIBRARY_ID") {
          contentType = MediaType.APPLICATION_JSON
          content = """
            {
              "name": "Library",
              "description": "",
              "owner": "${makeUuid()}"
            }
          """.trimIndent()
        }
        .andExpect {
          status { isForbidden() }
          jsonPath("$.result") { value("ERROR") }
          jsonPath("$.errors.length()") { value(1) }
          jsonPath("$.errors[0].id") { value("LibraryOwnerChangedException") }
        }
    }
  }
}