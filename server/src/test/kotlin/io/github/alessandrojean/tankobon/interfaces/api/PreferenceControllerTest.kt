package io.github.alessandrojean.tankobon.interfaces.api

import io.github.alessandrojean.tankobon.domain.model.Preference
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.ROLE_USER
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.PreferenceRepository
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
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class PreferenceControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val preferenceRepository: PreferenceRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val ADMIN_ID = "d3afd95e-7dda-467e-9cdd-c2996b59dde9"
    private const val USER_ID = "a68132c1-25d3-4f36-a1cf-ae674741d605"
  }

  private val admin = TankobonUser("admin@example.org", "", true, "Admin", id = ADMIN_ID)
  private val user = TankobonUser("user@example.org", "", false, "User", id = USER_ID)
  private val preference = Preference(USER_ID, "key", "value")

  @BeforeAll
  fun setup() {
    userRepository.insert(admin)
    userRepository.insert(user)
  }

  @AfterAll
  fun tearDown() {
    userRepository.deleteAll()
  }

  @AfterEach
  fun clear() {
    preferenceRepository.deleteAll()
  }

  @Nested
  inner class UnauthorizedUser {
    @Test
    @WithMockCustomUser(id = USER_ID, roles = [ROLE_USER])
    fun `it should return forbidden when a non-admin user tries to get the preferences from other user`() {
      mockMvc.get("/api/v1/users/$ADMIN_ID/preferences")
        .andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should return ok when getting the preferences from other user if the user is an admin`() {
      preferenceRepository.insert(preference)

      mockMvc.get("/api/v1/users/$USER_ID/preferences")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(1) }
          jsonPath("$.data[0].id") { value(preference.key) }
        }
    }
  }

  @Nested
  inner class Delete {
    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should return no content if an admin deletes a preference from any user`() {
      preferenceRepository.insert(preference)

      mockMvc.delete("/api/v1/users/$USER_ID/preferences/${preference.key}")
        .andExpect { status { isNoContent() } }

      mockMvc.get("/api/v1/users/$USER_ID/preferences")
        .andExpect {
          status { isOk() }
          jsonPath("$.result") { value("OK") }
          jsonPath("$.data.length()") { value(0) }
        }
    }

  }
}