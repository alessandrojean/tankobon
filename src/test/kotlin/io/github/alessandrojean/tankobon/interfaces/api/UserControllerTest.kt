package io.github.alessandrojean.tankobon.interfaces.api

import com.ninjasquad.springmockk.SpykBean
import io.github.alessandrojean.tankobon.domain.model.ROLE_ADMIN
import io.github.alessandrojean.tankobon.domain.model.ROLE_USER
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeUuid
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest(
  @Autowired private val mockMvc: MockMvc,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  companion object {
    private const val ADMIN_ID = "1f7c1ddf-844a-47cf-aa5c-c42c4ca86728"
  }

  @SpykBean
  private lateinit var userLifecycle: TankobonUserLifecycle

  private val admin = TankobonUser("admin@example.org", "", true, id = ADMIN_ID)

  @BeforeAll
  fun setup() {
    userRepository.insert(admin)
  }

  @AfterAll
  fun tearDown() {
    userRepository.findAll()
      .forEach(userLifecycle::deleteUser)
  }

  @AfterEach
  fun deleteUsers() {
    userRepository.findAll()
      .filterNot { it.email == admin.email }
      .forEach(userLifecycle::deleteUser)
  }

  @ParameterizedTest
  @ValueSource(strings = ["user", "user@domain"])
  @WithMockCustomUser(roles = [ROLE_ADMIN])
  fun `it should return bad request when creating a user with invalid email`(email: String) {
    val jsonString = """{"email": "$email", "password": "password"}"""

    mockMvc
      .post("/api/v1/users") {
        contentType = MediaType.APPLICATION_JSON
        content = jsonString
      }
      .andExpect {
        status { isBadRequest() }
      }
  }

  @Nested
  inner class Update {
    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should update the roles of an existing user without roles`() {
      val user = TankobonUser("user@example.org", "", false, id = makeUuid())
      userLifecycle.createUser(user)

      val jsonString = """
        {
          "email": "${user.email}",
          "roles": ["ROLE_$ROLE_ADMIN", "ROLE_$ROLE_USER"]
        }
      """.trimIndent()

      mockMvc
        .put("/api/v1/users/${user.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }
        .andExpect {
          status { isNoContent() }
        }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.isAdmin).isTrue
      }

      verify(exactly = 1) { userLifecycle.expireSessions(any()) }
    }

    @Test
    @WithMockCustomUser(id = ADMIN_ID, roles = [ROLE_ADMIN])
    fun `it should update the roles of an existing user with roles`() {
      val user = TankobonUser("user@example.org", "", true, id = makeUuid())
      userLifecycle.createUser(user)

      val jsonString = """
        {
          "email": "${user.email}",
          "roles": ["ROLE_$ROLE_USER"]
        }
      """.trimIndent()

      mockMvc
        .put("/api/v1/users/${user.id}") {
          contentType = MediaType.APPLICATION_JSON
          content = jsonString
        }
        .andExpect {
          status { isNoContent() }
        }

      with(userRepository.findByIdOrNull(user.id)) {
        assertThat(this).isNotNull
        assertThat(this!!.isAdmin).isFalse
      }

      verify(exactly = 1) { userLifecycle.expireSessions(any()) }
    }
  }
}