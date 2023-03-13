package io.github.alessandrojean.tankobon.infrastructure.jooq

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
import io.github.alessandrojean.tankobon.domain.model.Collection as DomainCollection

@ExtendWith(SpringExtension::class)
@SpringBootTest
class CollectionDaoTest(
  @Autowired private val collectionDao: CollectionDao,
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
  fun deleteCollections() {
    collectionDao.deleteAll()
    assertThat(collectionDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)
    val created = collectionDao.findById(collection.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(collection.name)
      assertThat(description).isEqualTo(collection.description)
      assertThat(libraryId).isEqualTo(collection.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing collection`() {
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )
    collectionDao.insert(collection)

    val modificationDate = LocalDateTime.now()

    val updated = collectionDao.findById(collection.id).copy(
      name = "CollectionUpdated",
      description = "CollectionUpdated description"
    )

    collectionDao.update(updated)
    val modified = collectionDao.findById(updated.id)

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
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)
    assertThat(collectionDao.count()).isEqualTo(1)

    collectionDao.delete(collection.id)
    assertThat(collectionDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all collections`() {
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )
    val collection2 = DomainCollection(
      name = "Collection2",
      description = "Collection2 description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)
    collectionDao.insert(collection2)
    assertThat(collectionDao.count()).isEqualTo(2)

    collectionDao.deleteAll()
    assertThat(collectionDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all collections`() {
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )
    val collection2 = DomainCollection(
      name = "Collection2",
      description = "Collection2 description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)
    collectionDao.insert(collection2)

    val all = collectionDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(DomainCollection::name))
      .containsExactlyInAnyOrder("Collection", "Collection2")
  }

  @Test
  fun `it should find and return all collections by its id's`() {
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )
    val collection2 = DomainCollection(
      name = "Collection2",
      description = "Collection2 description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)
    collectionDao.insert(collection2)

    val all = collectionDao.findAllByIds(listOf(collection.id, collection2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(DomainCollection::name))
      .containsExactlyInAnyOrder("Collection", "Collection2")
  }

  @Test
  fun `it should find by id and return a collection`() {
    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )

    collectionDao.insert(collection)

    val found = collectionDao.findByIdOrNull(collection.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Collection")
  }

  @Test
  fun `it should return null when finding a non-existent collection`() {
    val found = collectionDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the collections from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val collection = DomainCollection(
      name = "Collection",
      description = "Collection description",
      libraryId = library.id,
    )
    val collection2 = DomainCollection(
      name = "Collection2",
      description = "Collection2 description",
      libraryId = library2.id,
    )

    collectionDao.insert(collection)
    collectionDao.insert(collection2)

    val all = collectionDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(DomainCollection::name))
      .containsExactlyInAnyOrder("Collection")
  }
}
