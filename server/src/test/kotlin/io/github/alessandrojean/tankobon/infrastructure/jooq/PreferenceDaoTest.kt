package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Preference
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class PreferenceDaoTest(
  @Autowired private val preferenceDao: PreferenceDao,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val user = TankobonUser("user@example.org", "", false, "User", id = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(user)
  }

  @AfterAll
  fun tearDown() {
    userRepository.deleteAll()
  }

  @AfterEach
  fun deletePreferences() {
    preferenceDao.deleteAll()
  }

  @Test
  fun `it should persist when inserting`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )

    preferenceDao.insert(preference)
    val created = preferenceDao.findByKeyFromUser(preference.key, preference.userId)!!

    with(created) {
      assertThat(userId).isEqualTo(preference.userId)
      assertThat(key).isEqualTo(preference.key)
      assertThat(value).isEqualTo(preference.value)
    }
  }

  @Test
  fun `it should persist when updating an existing preference`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )
    preferenceDao.insert(preference)

    val updated = preferenceDao.findByKeyFromUser(preference.key, preference.userId)!!.copy(
      value = "valueUpdated",
    )

    preferenceDao.update(updated)
    val modified = preferenceDao.findByKeyFromUser(preference.key, preference.userId)!!

    with(modified) {
      assertThat(value).isEqualTo(updated.value)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )

    preferenceDao.insert(preference)
    assertThat(preferenceDao.count()).isEqualTo(1)

    preferenceDao.delete(preference.key, preference.userId)
    assertThat(preferenceDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all preferences`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )
    val preference2 = Preference(
      userId = user.id,
      key = "key2",
      value = "value2",
    )

    preferenceDao.insert(preference)
    preferenceDao.insert(preference2)
    assertThat(preferenceDao.count()).isEqualTo(2)

    preferenceDao.deleteAll()
    assertThat(preferenceDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all preferences from an user`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )
    val preference2 = Preference(
      userId = user.id,
      key = "key2",
      value = "value2",
    )

    preferenceDao.insert(preference)
    preferenceDao.insert(preference2)

    val all = preferenceDao.findAllByUser(user.id)
    assertThat(all).hasSize(2)
    assertThat(all.map(Preference::key))
      .containsExactlyInAnyOrder("key", "key2")
  }

  @Test
  fun `it should find by key and return a preference`() {
    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )

    preferenceDao.insert(preference)

    val found = preferenceDao.findByKeyFromUser(preference.key, preference.userId)
    assertThat(found).isNotNull
    assertThat(found?.key).isEqualTo("key")
  }

  @Test
  fun `it should return null when finding a non-existent preference`() {
    val found = preferenceDao.findByKeyFromUser("random_key", user.id)
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the tags from an user`() {
    val user2 = TankobonUser("user2@example.org", "", false, "User2")
    userRepository.insert(user2)

    val preference = Preference(
      userId = user.id,
      key = "key",
      value = "value",
    )
    val preference2 = Preference(
      userId = user2.id,
      key = "key2",
      value = "value2",
    )

    preferenceDao.insert(preference)
    preferenceDao.insert(preference2)

    val all = preferenceDao.findAllByUser(user.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Preference::key))
      .containsExactlyInAnyOrder("key")
  }
}
