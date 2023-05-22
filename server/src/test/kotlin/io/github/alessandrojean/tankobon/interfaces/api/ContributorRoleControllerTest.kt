package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.ContributorRoleLifecycle
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
class ContributorRoleControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val contributorRoleRepository: ContributorRoleRepository,
  @Autowired private val contributorRoleLifecycle: ContributorRoleLifecycle,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val ADMIN_ID = "b1f00dfa-e51f-41d0-a995-857e533ed286"
    private const val OWNER_ID = "6c34b8ee-a370-49d5-866d-b65e4c3734ad"
    private const val USER_ID = "b0c3e382-aa11-4c48-bf11-2b49832d4a7d"
    private const val LIBRARY_ID = "4c6e068e-c01e-4356-8586-64ab072a7eb5"
    private const val CONTRIBUTOR_ROLE_ID = "650e423b-1fab-4a9f-bf28-f0be39548a16"
  }

  private val admin = TankobonUser("admin@example.org", "", true, "Admin", id = ADMIN_ID)
  private val owner = TankobonUser("user@example.org", "", true, "User", id = OWNER_ID)
  private val user = TankobonUser("user2@example.org", "", false, "User2", id = USER_ID)
  private val library = makeLibrary("Library", "", id = LIBRARY_ID, ownerId = OWNER_ID)
  private val contributorRole = ContributorRole("ContributorRole", id = CONTRIBUTOR_ROLE_ID, libraryId = LIBRARY_ID)

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
    contributorRoleRepository.deleteAll()
  }

  @Nested
  inner class UnauthorizedUser {
    @Test
    fun `it should return unauthorized when getting the contributor roles from a library with an anonymous user`() {
      mockMvc.get("/api/v1/libraries/${library.id}/contributor-roles")
        .andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithMockCustomUser(id = USER_ID)
    fun `it should return forbidden when getting the contributor roles from a library the user does not have access`() {
      mockMvc.get("/api/v1/libraries/${library.id}/contributor-roles")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(id = OWNER_ID, roles = [ROLE_ADMIN])
    fun `it should return ok when getting the contributor roles from a library if the user is an admin`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.get("/api/v1/libraries/${library.id}/contributor-roles")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(contributorRole.id) }
        }
    }
  }

  @Nested
  inner class DuplicateNames {
    @Test
    @WithMockCustomUser(id = OWNER_ID)
    fun `it should return bad request when creating a contributor role with a duplicate name in the library`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      val jsonString = """
        {
          "name": "${contributorRole.name.lowercase()}",
          "description": "",
          "library": "${library.id}"
        }
      """.trimIndent()

      mockMvc
        .post("/api/v1/contributor-roles") {
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
    fun `it should return forbidden if a non-admin user tries to delete a contributor role from a library it does not have access`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.delete("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a contributor role from any user`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.delete("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isNotFound() } }
    }
  }
}
