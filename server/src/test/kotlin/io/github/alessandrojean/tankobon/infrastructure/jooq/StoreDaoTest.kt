package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Store
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
class StoreDaoTest(
  @Autowired private val storeDao: StoreDao,
  @Autowired private val libraryRepository: LibraryRepository,
  @Autowired private val userRepository: TankobonUserRepository,
) {

  private val user = TankobonUser("user@example.org", "", false, "User", id = "1")
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
  fun deleteStores() {
    storeDao.deleteAll()
    assertThat(storeDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )

    storeDao.insert(store)
    val created = storeDao.findById(store.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(store.name)
      assertThat(description).isEqualTo(store.description)
      assertThat(libraryId).isEqualTo(store.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing store`() {
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )
    storeDao.insert(store)

    val modificationDate = LocalDateTime.now()

    val updated = storeDao.findById(store.id).copy(
      name = "StoreUpdated",
      description = "StoreUpdated description"
    )

    storeDao.update(updated)
    val modified = storeDao.findById(updated.id)

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
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )

    storeDao.insert(store)
    assertThat(storeDao.count()).isEqualTo(1)

    storeDao.delete(store.id)
    assertThat(storeDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all stores`() {
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )
    val store2 = Store(
      name = "Store2",
      description = "Store2 description",
      libraryId = library.id,
    )

    storeDao.insert(store)
    storeDao.insert(store2)
    assertThat(storeDao.count()).isEqualTo(2)

    storeDao.deleteAll()
    assertThat(storeDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all stores`() {
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )
    val store2 = Store(
      name = "Store2",
      description = "Store2 description",
      libraryId = library.id,
    )

    storeDao.insert(store)
    storeDao.insert(store2)

    val all = storeDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Store::name))
      .containsExactlyInAnyOrder("Store", "Store2")
  }

  @Test
  fun `it should find and return all stores by its id's`() {
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )
    val store2 = Store(
      name = "Store2",
      description = "Store2 description",
      libraryId = library.id,
    )

    storeDao.insert(store)
    storeDao.insert(store2)

    val all = storeDao.findAllByIds(listOf(store.id, store2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Store::name))
      .containsExactlyInAnyOrder("Store", "Store2")
  }

  @Test
  fun `it should find by id and return a store`() {
    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )

    storeDao.insert(store)

    val found = storeDao.findByIdOrNull(store.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Store")
  }

  @Test
  fun `it should return null when finding a non-existent store`() {
    val found = storeDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the stores from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val store = Store(
      name = "Store",
      description = "Store description",
      libraryId = library.id,
    )
    val store2 = Store(
      name = "Store2",
      description = "Store2 description",
      libraryId = library2.id,
    )

    storeDao.insert(store)
    storeDao.insert(store2)

    val all = storeDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Store::name))
      .containsExactlyInAnyOrder("Store")
  }
}
