package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Person
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
class PersonDaoTest(
  @Autowired private val personDao: PersonDao,
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
  fun deletePersons() {
    personDao.deleteAll()
    assertThat(personDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )

    personDao.insert(person)
    val created = personDao.findById(person.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(person.name)
      assertThat(description).isEqualTo(person.description)
      assertThat(libraryId).isEqualTo(person.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing person`() {
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )
    personDao.insert(person)

    val modificationDate = LocalDateTime.now()

    val updated = personDao.findById(person.id).copy(
      name = "PersonUpdated",
      description = "PersonUpdated description"
    )

    personDao.update(updated)
    val modified = personDao.findById(updated.id)

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
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )

    personDao.insert(person)
    assertThat(personDao.count()).isEqualTo(1)

    personDao.delete(person.id)
    assertThat(personDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all persons`() {
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )
    val person2 = Person(
      name = "Person2",
      description = "Person2 description",
      libraryId = library.id,
    )

    personDao.insert(person)
    personDao.insert(person2)
    assertThat(personDao.count()).isEqualTo(2)

    personDao.deleteAll()
    assertThat(personDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all persons`() {
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )
    val person2 = Person(
      name = "Person2",
      description = "Person2 description",
      libraryId = library.id,
    )

    personDao.insert(person)
    personDao.insert(person2)

    val all = personDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Person::name))
      .containsExactlyInAnyOrder("Person", "Person2")
  }

  @Test
  fun `it should find and return all persons by its id's`() {
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )
    val person2 = Person(
      name = "Person2",
      description = "Person2 description",
      libraryId = library.id,
    )

    personDao.insert(person)
    personDao.insert(person2)

    val all = personDao.findAllByIds(listOf(person.id, person2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Person::name))
      .containsExactlyInAnyOrder("Person", "Person2")
  }

  @Test
  fun `it should find by id and return a person`() {
    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )

    personDao.insert(person)

    val found = personDao.findByIdOrNull(person.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Person")
  }

  @Test
  fun `it should return null when finding a non-existent person`() {
    val found = personDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the persons from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val person = Person(
      name = "Person",
      description = "Person description",
      libraryId = library.id,
    )
    val person2 = Person(
      name = "Person2",
      description = "Person2 description",
      libraryId = library2.id,
    )

    personDao.insert(person)
    personDao.insert(person2)

    val all = personDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Person::name))
      .containsExactlyInAnyOrder("Person")
  }
}
