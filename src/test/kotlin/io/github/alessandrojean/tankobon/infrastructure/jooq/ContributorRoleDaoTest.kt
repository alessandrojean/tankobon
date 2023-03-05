package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.makeLibrary
import io.github.alessandrojean.tankobon.domain.persistence.LibraryRepository
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
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ContributorRoleDaoTest(
  @Autowired private val contributorRoleDao: ContributorRoleDao,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val user = TankobonUser("user@example.org", "", false, id = "1")
  private val library = makeLibrary("Library", "", id = "1", ownerId = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(user)
    libraryRepository.insert(library)
  }

  @AfterAll
  fun tearDown() {
    userRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @AfterEach
  fun deleteContributorRoles() {
    contributorRoleDao.deleteAll()
    assertThat(contributorRoleDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)
    val created = contributorRoleDao.findById(contributorRole.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(contributorRole.name)
      assertThat(description).isEqualTo(contributorRole.description)
      assertThat(libraryId).isEqualTo(contributorRole.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing contributorRole`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )
    contributorRoleDao.insert(contributorRole)

    val modificationDate = LocalDateTime.now()

    val updated = contributorRoleDao.findById(contributorRole.id).copy(
      name = "ContributorRoleUpdated",
      description = "ContributorRoleUpdated description"
    )

    contributorRoleDao.update(updated)
    val modified = contributorRoleDao.findById(updated.id)

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
      assertThat(name).isEqualTo(updated.name)
      assertThat(description).isEqualTo(updated.description)
      assertThat(libraryId).isEqualTo(updated.libraryId)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)
    assertThat(contributorRoleDao.count()).isEqualTo(1)

    contributorRoleDao.delete(contributorRole.id)
    assertThat(contributorRoleDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all contributorRoles`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )
    val contributorRole2 = ContributorRole(
      name = "ContributorRole2",
      description = "ContributorRole2 description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)
    contributorRoleDao.insert(contributorRole2)
    assertThat(contributorRoleDao.count()).isEqualTo(2)

    contributorRoleDao.deleteAll()
    assertThat(contributorRoleDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all contributorRoles`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )
    val contributorRole2 = ContributorRole(
      name = "ContributorRole2",
      description = "ContributorRole2 description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)
    contributorRoleDao.insert(contributorRole2)

    val all = contributorRoleDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(ContributorRole::name))
      .containsExactlyInAnyOrder("ContributorRole", "ContributorRole2")
  }

  @Test
  fun `it should find and return all contributorRoles by its id's`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )
    val contributorRole2 = ContributorRole(
      name = "ContributorRole2",
      description = "ContributorRole2 description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)
    contributorRoleDao.insert(contributorRole2)

    val all = contributorRoleDao.findAllByIds(listOf(contributorRole.id, contributorRole2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(ContributorRole::name))
      .containsExactlyInAnyOrder("ContributorRole", "ContributorRole2")
  }

  @Test
  fun `it should find by id and return a contributorRole`() {
    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )

    contributorRoleDao.insert(contributorRole)

    val found = contributorRoleDao.findByIdOrNull(contributorRole.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("ContributorRole")
  }

  @Test
  fun `it should return null when finding a non-existent contributorRole`() {
    val found = contributorRoleDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the contributorRoles from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val contributorRole = ContributorRole(
      name = "ContributorRole",
      description = "ContributorRole description",
      libraryId = library.id,
    )
    val contributorRole2 = ContributorRole(
      name = "ContributorRole2",
      description = "ContributorRole2 description",
      libraryId = library2.id,
    )

    contributorRoleDao.insert(contributorRole)
    contributorRoleDao.insert(contributorRole2)

    val all = contributorRoleDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(ContributorRole::name))
      .containsExactlyInAnyOrder("ContributorRole")
  }
}
