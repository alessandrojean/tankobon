package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.PublisherLinks
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
class PublisherDaoTest(
  @Autowired private val publisherDao: PublisherDao,
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
  fun deletePublishers() {
    publisherDao.deleteAll()
    assertThat(publisherDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      links = PublisherLinks(twitter = "publisher"),
      legalName = "Publisher LLC",
      location = "Location",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)
    val created = publisherDao.findById(publisher.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(publisher.name)
      assertThat(description).isEqualTo(publisher.description)
      assertThat(links.twitter).isEqualTo(publisher.links.twitter)
      assertThat(legalName).isEqualTo(publisher.legalName)
      assertThat(location).isEqualTo(publisher.location)
      assertThat(libraryId).isEqualTo(publisher.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing publisher`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      links = PublisherLinks(twitter = "publisher"),
      legalName = "Publisher LLC",
      location = "Location",
      libraryId = library.id,
    )
    publisherDao.insert(publisher)

    val modificationDate = LocalDateTime.now()

    val updated = publisherDao.findById(publisher.id).copy(
      name = "PublisherUpdated",
      description = "PublisherUpdated description",
      links = PublisherLinks(twitter = "publisher_"),
      legalName = "Publisher Inc",
      location = "Another location",
    )

    publisherDao.update(updated)
    val modified = publisherDao.findById(updated.id)

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
      assertThat(name).isEqualTo(updated.name)
      assertThat(description).isEqualTo(updated.description)
      assertThat(links.twitter).isEqualTo(updated.links.twitter)
      assertThat(legalName).isEqualTo(updated.legalName)
      assertThat(location).isEqualTo(updated.location)
      assertThat(libraryId).isEqualTo(updated.libraryId)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)
    assertThat(publisherDao.count()).isEqualTo(1)

    publisherDao.delete(publisher.id)
    assertThat(publisherDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all publishers`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )
    val publisher2 = Publisher(
      name = "Publisher2",
      description = "Publisher2 description",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)
    publisherDao.insert(publisher2)
    assertThat(publisherDao.count()).isEqualTo(2)

    publisherDao.deleteAll()
    assertThat(publisherDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all publishers`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )
    val publisher2 = Publisher(
      name = "Publisher2",
      description = "Publisher2 description",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)
    publisherDao.insert(publisher2)

    val all = publisherDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Publisher::name))
      .containsExactlyInAnyOrder("Publisher", "Publisher2")
  }

  @Test
  fun `it should find and return all publishers by its id's`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )
    val publisher2 = Publisher(
      name = "Publisher2",
      description = "Publisher2 description",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)
    publisherDao.insert(publisher2)

    val all = publisherDao.findAllByIds(listOf(publisher.id, publisher2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Publisher::name))
      .containsExactlyInAnyOrder("Publisher", "Publisher2")
  }

  @Test
  fun `it should find by id and return a publisher`() {
    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )

    publisherDao.insert(publisher)

    val found = publisherDao.findByIdOrNull(publisher.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Publisher")
  }

  @Test
  fun `it should return null when finding a non-existent publisher`() {
    val found = publisherDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the publishers from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val publisher = Publisher(
      name = "Publisher",
      description = "Publisher description",
      libraryId = library.id,
    )
    val publisher2 = Publisher(
      name = "Publisher2",
      description = "Publisher2 description",
      libraryId = library2.id,
    )

    publisherDao.insert(publisher)
    publisherDao.insert(publisher2)

    val all = publisherDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Publisher::name))
      .containsExactlyInAnyOrder("Publisher")
  }
}
