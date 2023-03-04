package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class TankobonUserDaoTest(
  @Autowired private val tankobonUserDao: TankobonUserDao,
) {

  @AfterEach
  fun deleteUsers() {
    tankobonUserDao.deleteAll()
    assertThat(tankobonUserDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist the user when saving`() {
    val now = LocalDateTime.now()
    val user = TankobonUser(
      email = "user@example.org",
      password = "password",
      isAdmin = false,
    )

    tankobonUserDao.insert(user)
    val created = tankobonUserDao.findByIdOrNull(user.id)!!

    with(created) {
      assertThat(id).isNotEmpty
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(email).isEqualTo("user@example.org")
      assertThat(password).isEqualTo("password")
      assertThat(isAdmin).isFalse
    }
  }

  @Test
  fun `it should persist the user when modifying and saving`() {
    val user = TankobonUser(
      email = "user@example.org",
      password = "password",
      isAdmin = false,
    )

    tankobonUserDao.insert(user)
    val created = tankobonUserDao.findByIdOrNull(user.id)!!

    val modified = created.copy(
      email = "user2@example.org",
      password = "password2",
      isAdmin = true,
    )
    val modifiedDate = LocalDateTime.now()
    tankobonUserDao.update(modified)
    val modifiedSaved = tankobonUserDao.findByIdOrNull(modified.id)!!

    with(modifiedSaved) {
      assertThat(id).isEqualTo(created.id)
      assertThat(createdAt).isEqualTo(created.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modifiedDate, offset)
        .isNotEqualTo(modified.createdAt)
      assertThat(email).isEqualTo("user2@example.org")
      assertThat(password).isEqualTo("password2")
      assertThat(isAdmin).isTrue
    }
  }

  @Test
  fun `it should persist multiple users when saving`() {
    tankobonUserDao.insert(TankobonUser("user1@example.org", "p", false))
    tankobonUserDao.insert(TankobonUser("user2@example.org", "p", true))

    val users = tankobonUserDao.findAll()

    assertThat(users).hasSize(2)
    assertThat(users.map(TankobonUser::email))
      .containsExactlyInAnyOrder("user1@example.org", "user2@example.org")
  }

  @Test
  fun `it should count properly`() {
    tankobonUserDao.insert(TankobonUser("user1@example.org", "p", false))
    tankobonUserDao.insert(TankobonUser("user2@example.org", "p", true))

    val count = tankobonUserDao.count()
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `it should find an existing user by its id`() {
    val existing = TankobonUser("user1@example.org", "p", false)
    tankobonUserDao.insert(existing)

    val user = tankobonUserDao.findByIdOrNull(existing.id)
    assertThat(user).isNotNull
  }

  @Test
  fun `it should return null when finding a non-existing user`() {
    val user = tankobonUserDao.findByIdOrNull("12345")
    assertThat(user).isNull()
  }

  @Test
  fun `it should persist when deleting the user`() {
    val existing = TankobonUser("user1@example.org", "p", false)
    tankobonUserDao.insert(existing)

    tankobonUserDao.delete(existing.id)
    assertThat(tankobonUserDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should check properly if email already exists even with different casing`() {
    tankobonUserDao.insert(
      TankobonUser("user1@example.org", "p", false)
    )

    val exists = tankobonUserDao.existsByEmailIgnoreCase("USER1@EXAMPLE.ORG")
    val notExists = tankobonUserDao.existsByEmailIgnoreCase("USER2@EXAMPLE.ORG")

    assertThat(exists).isTrue
    assertThat(notExists).isFalse
  }

  @Test
  fun `it should find the user by its email if it exists even with different casing`() {
    tankobonUserDao.insert(
      TankobonUser("user1@example.org", "p", false)
    )

    val found = tankobonUserDao.findByEmailIgnoreCaseOrNull("USER1@EXAMPLE.ORG")
    val notFound = tankobonUserDao.findByEmailIgnoreCaseOrNull("USER2@EXAMPLE.ORG")

    assertThat(found).isNotNull
    assertThat(found?.email).isEqualTo("user1@example.org")
    assertThat(notFound).isNull()
  }
}