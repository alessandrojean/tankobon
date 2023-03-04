package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Library
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.TankobonUserRepository
import io.github.alessandrojean.tankobon.domain.service.TankobonUserLifecycle
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
class LibraryDaoTest(
  @Autowired private val libraryDao: LibraryDao,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val user = TankobonUser("user@example.org", "", false, id = "1")

  @BeforeAll
  fun setup() {
    userRepository.insert(user)
  }

  @AfterAll
  fun tearDown() {
    userRepository.deleteAll()
  }

  @AfterEach
  fun deleteLibraries() {
    libraryDao.deleteAll()
    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )

    libraryDao.insert(library)
    val created = libraryDao.findById(library.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(library.name)
      assertThat(description).isEqualTo(library.description)
    }
  }

  @Test
  fun `it should persist when updating an existing library`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )
    libraryDao.insert(library)

    val modificationDate = LocalDateTime.now()

    val updated = libraryDao.findById(library.id).copy(
      name = "LibraryUpdated",
      description = "LibraryUpdated description"
    )

    libraryDao.update(updated)
    val modified = libraryDao.findById(updated.id)

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
      assertThat(name).isEqualTo(updated.name)
      assertThat(description).isEqualTo(updated.description)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )

    libraryDao.insert(library)
    assertThat(libraryDao.count()).isEqualTo(1)

    libraryDao.delete(library.id)
    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all libraries`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )
    val library2 = Library(
      name = "Library2",
      description = "Library2 description",
      ownerId = user.id,
    )

    libraryDao.insert(library)
    libraryDao.insert(library2)
    assertThat(libraryDao.count()).isEqualTo(2)

    libraryDao.deleteAll()
    assertThat(libraryDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all libraries`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )
    val library2 = Library(
      name = "Library2",
      description = "Library2 description",
      ownerId = user.id,
    )

    libraryDao.insert(library)
    libraryDao.insert(library2)

    val all = libraryDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Library::name))
      .containsExactlyInAnyOrder("Library", "Library2")
  }

  @Test
  fun `it should find and return all libraries by its id's`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )
    val library2 = Library(
      name = "Library2",
      description = "Library2 description",
      ownerId = user.id,
    )

    libraryDao.insert(library)
    libraryDao.insert(library2)

    val all = libraryDao.findAllByIds(listOf(library.id, library2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Library::name))
      .containsExactlyInAnyOrder("Library", "Library2")
  }

  @Test
  fun `it should find by id and return an existing library`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
    )

    libraryDao.insert(library)

    val found = libraryDao.findByIdOrNull(library.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Library")
  }

  @Test
  fun `it should return null when finding a non-existent library`() {
    val found = libraryDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should find and return all libraries from the user and the shared ones`() {
    val library = Library(
      name = "Library",
      description = "Library description",
      ownerId = user.id,
      id = "1",
    )
    libraryDao.insert(library)

    val otherUser = TankobonUser("user2@example.org", "", false, id = "2")
    userRepository.insert(otherUser)

    with(libraryDao.findByOwnerIdIncludingShared(otherUser.id)) {
      assertThat(this).isEmpty()
    }

    val library2 = Library(
      name = "Library 2",
      description = "Library 2 description",
      ownerId = otherUser.id,
      id = "2",
    )
    libraryDao.insert(library2)

    with(libraryDao.findByOwnerIdIncludingShared(otherUser.id)) {
      assertThat(this).hasSize(1)
      assertThat(map { it.ownerId }).containsExactlyInAnyOrder("2")
    }

    val library2Modified = library2.copy(sharedUsersIds = setOf(user.id))
    libraryDao.update(library2Modified)

    with(libraryDao.findByOwnerIdIncludingShared(user.id)) {
      assertThat(this).hasSize(2)
      assertThat(map { it.ownerId }).containsExactlyInAnyOrder("1", "2")
    }
  }
}
