package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.Tag
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TagRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.TagLifecycle
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
class TagControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val tagRepository: TagRepository,
  @Autowired private val tagLifecycle: TagLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val OWNER_ID = "4d3f1893-ce2b-42f2-aadd-f692356257dc"
    private const val USER_ID = "a68132c1-25d3-4f36-a1cf-ae674741d605"
    private const val LIBRARY_ID = "2a9ad6ce-72ba-457a-9c98-c116708efabf"
    private const val TAG_ID = "dedb1ebd-24ae-43e8-a24d-810b1f9e2c16"
  }

  private val owner = TankobonUser("user@example.org", "", true, "User", id = OWNER_ID)
  private val user = TankobonUser("user2@example.org", "", false, "User2", id = USER_ID)
  private val library = makeLibrary("Library", "", id = LIBRARY_ID, ownerId = OWNER_ID)
  private val tag = Tag("Tag", id = TAG_ID, libraryId = LIBRARY_ID)

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
    tagRepository.deleteAll()
  }

  @Nested
  inner class UnauthorizedUser {
    @Test
    fun `it should return unauthorized when getting the tags from a library with an anonymous user`() {
      mockMvc.get("/api/v1/libraries/${library.id}/tags")
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithMockCustomUser(id = USER_ID)
    fun `it should return forbidden when getting the tags from a library the user does not have access`() {
      mockMvc.get("/api/v1/libraries/${library.id}/tags")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return ok when getting the tags from a library if the user is an admin`() {
      tagLifecycle.addTag(tag)

      mockMvc.get("/api/v1/libraries/${library.id}/tags")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(tag.id) }
        }
    }
  }

  @Nested
  inner class DuplicateNames {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request when creating a tag with a duplicate name in the library`() {
      tagLifecycle.addTag(tag)

      val jsonString = """
        {
          "name": "${tag.name.lowercase()}",
          "description": "",
          "library": "${library.id}"
        }
      """.trimIndent()

      mockMvc
        .post("/api/v1/tags") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }
        .andExpect { status { isBadRequest() } }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser(id = USER_ID)
    fun `it should return forbidden if a non-admin user tries to delete a tag from a library it does not have access`() {
      tagLifecycle.addTag(tag)

      mockMvc.delete("/api/v1/tags/${tag.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a tag from any user`() {
      tagLifecycle.addTag(tag)

      mockMvc.delete("/api/v1/tags/${tag.id}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/tags/${tag.id}")
        .andExpect { status { isNotFound() } }
    }

  }
}