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

  private val owner = TankobonUser("user@example.org", "", true, id = "1")
  private val user = TankobonUser("user2@example.org", "", false, id = "2")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")
  private val contributorRole = ContributorRole("ContributorRole", id = "1", libraryId = "1")

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
    @WithMockCustomUser(id = "2")
    fun `it should return forbidden when getting the contributor roles from a library the user does not have access`() {
      mockMvc.get("/api/v1/libraries/${library.id}/contributor-roles")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return ok when getting the contributor roles from a library if the user is an admin`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.get("/api/v1/libraries/${library.id}/contributor-roles")
        .andExpect {
          status { isOk() }
          jsonPath("$.length()") { value(1) }
          jsonPath("$.[0].id") { value(contributorRole.id) }
        }
    }
  }

  @Nested
  inner class DuplicateNames {
    @Test
    @WithMockCustomUser(id = "1")
    fun `it should return bad request when creating a contributor role with a duplicate name in the library`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      val jsonString = """{"name": "${contributorRole.name.lowercase()}", "description": ""}"""

      mockMvc
        .post("/api/v1/libraries/${library.id}/contributor-roles") {
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
    fun `it should return forbidden if a non-admin user tries to delete a contributor role from a library it does not have access`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.delete("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a contributor role from any user`() {
      contributorRoleLifecycle.addContributorRole(contributorRole)

      mockMvc.delete("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/contributor-roles/${contributorRole.id}")
        .andExpect { status { isNotFound() } }
    }

  }
}