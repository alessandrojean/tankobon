package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Tag
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
class TagDaoTest(
  @Autowired private val tagDao: TagDao,
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
  fun deleteTags() {
    tagDao.deleteAll()
    assertThat(tagDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )

    tagDao.insert(tag)
    val created = tagDao.findById(tag.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(tag.name)
      assertThat(description).isEqualTo(tag.description)
      assertThat(libraryId).isEqualTo(tag.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing tag`() {
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )
    tagDao.insert(tag)

    val modificationDate = LocalDateTime.now()

    val updated = tagDao.findById(tag.id).copy(
      name = "TagUpdated",
      description = "TagUpdated description"
    )

    tagDao.update(updated)
    val modified = tagDao.findById(updated.id)

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
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )

    tagDao.insert(tag)
    assertThat(tagDao.count()).isEqualTo(1)

    tagDao.delete(tag.id)
    assertThat(tagDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all tags`() {
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )
    val tag2 = Tag(
      name = "Tag2",
      description = "Tag2 description",
      libraryId = library.id,
    )

    tagDao.insert(tag)
    tagDao.insert(tag2)
    assertThat(tagDao.count()).isEqualTo(2)

    tagDao.deleteAll()
    assertThat(tagDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all tags`() {
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )
    val tag2 = Tag(
      name = "Tag2",
      description = "Tag2 description",
      libraryId = library.id,
    )

    tagDao.insert(tag)
    tagDao.insert(tag2)

    val all = tagDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Tag::name))
      .containsExactlyInAnyOrder("Tag", "Tag2")
  }

  @Test
  fun `it should find and return all tags by its id's`() {
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )
    val tag2 = Tag(
      name = "Tag2",
      description = "Tag2 description",
      libraryId = library.id,
    )

    tagDao.insert(tag)
    tagDao.insert(tag2)

    val all = tagDao.findAllByIds(listOf(tag.id, tag2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Tag::name))
      .containsExactlyInAnyOrder("Tag", "Tag2")
  }

  @Test
  fun `it should find by id and return a tag`() {
    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )

    tagDao.insert(tag)

    val found = tagDao.findByIdOrNull(tag.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Tag")
  }

  @Test
  fun `it should return null when finding a non-existent tag`() {
    val found = tagDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the tags from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val tag = Tag(
      name = "Tag",
      description = "Tag description",
      libraryId = library.id,
    )
    val tag2 = Tag(
      name = "Tag2",
      description = "Tag2 description",
      libraryId = library2.id,
    )

    tagDao.insert(tag)
    tagDao.insert(tag2)

    val all = tagDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Tag::name))
      .containsExactlyInAnyOrder("Tag")
  }
}
