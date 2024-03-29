package io.github.alessandrojean.tankobon.infrastructure.jooq

import io.github.alessandrojean.tankobon.domain.model.Series
import io.github.alessandrojean.tankobon.domain.model.SeriesAlternativeName
import io.github.alessandrojean.tankobon.domain.model.SeriesLinks
import io.github.alessandrojean.tankobon.domain.model.SeriesType
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
class SeriesDaoTest(
  @Autowired private val seriesDao: SeriesDao,
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
  fun deleteSeries() {
    seriesDao.deleteAll()
    assertThat(seriesDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when inserting`() {
    val now = LocalDateTime.now()
    val series = Series(
      name = "Series",
      description = "Series description",
      type = SeriesType.MANGA,
      alternativeNames = listOf(SeriesAlternativeName("The series", "en-US")),
      lastNumber = "2",
      originalLanguage = "en-US",
      links = SeriesLinks(twitter = "series"),
      libraryId = library.id,
    )

    seriesDao.insert(series)
    val created = seriesDao.findById(series.id)

    with(created) {
      assertThat(createdAt).isCloseTo(now, offset)
      assertThat(modifiedAt).isCloseTo(now, offset)
      assertThat(name).isEqualTo(series.name)
      assertThat(description).isEqualTo(series.description)
      assertThat(type).isEqualTo(series.type)
      assertThat(lastNumber).isEqualTo(series.lastNumber)
      assertThat(originalLanguage).isEqualTo(series.originalLanguage)
      assertThat(alternativeNames).hasSize(series.alternativeNames.size)
      assertThat(alternativeNames[0].name).isEqualTo(series.alternativeNames[0].name)
      assertThat(alternativeNames[0].language).isEqualTo(series.alternativeNames[0].language)
      assertThat(links.twitter).isEqualTo(series.links.twitter)
      assertThat(libraryId).isEqualTo(series.libraryId)
    }
  }

  @Test
  fun `it should persist when updating an existing series`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      type = SeriesType.MANGA,
      alternativeNames = listOf(SeriesAlternativeName("The series", "en-US")),
      lastNumber = "2",
      originalLanguage = "en-US",
      links = SeriesLinks(twitter = "series"),
      libraryId = library.id,
    )
    seriesDao.insert(series)

    val modificationDate = LocalDateTime.now()

    val updated = seriesDao.findById(series.id).copy(
      name = "SeriesUpdated",
      description = "SeriesUpdated description",
      type = SeriesType.MANHWA,
      alternativeNames = emptyList(),
      lastNumber = "3",
      originalLanguage = "en-UK",
      links = SeriesLinks(twitter = "series_"),
    )

    seriesDao.update(updated)
    val modified = seriesDao.findById(updated.id)

    with(modified) {
      assertThat(id).isEqualTo(updated.id)
      assertThat(createdAt).isEqualTo(updated.createdAt)
      assertThat(modifiedAt)
        .isCloseTo(modificationDate, offset)
        .isNotEqualTo(updated.modifiedAt)
      assertThat(name).isEqualTo(updated.name)
      assertThat(description).isEqualTo(updated.description)
      assertThat(type).isEqualTo(updated.type)
      assertThat(lastNumber).isEqualTo(updated.lastNumber)
      assertThat(originalLanguage).isEqualTo(updated.originalLanguage)
      assertThat(alternativeNames).hasSize(updated.alternativeNames.size)
      assertThat(links.twitter).isEqualTo(updated.links.twitter)
      assertThat(libraryId).isEqualTo(updated.libraryId)
    }
  }

  @Test
  fun `it should persist when deleting`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )

    seriesDao.insert(series)
    assertThat(seriesDao.count()).isEqualTo(1)

    seriesDao.delete(series.id)
    assertThat(seriesDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should persist when deleting all series`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )
    val series2 = Series(
      name = "Series2",
      description = "Series2 description",
      libraryId = library.id,
    )

    seriesDao.insert(series)
    seriesDao.insert(series2)
    assertThat(seriesDao.count()).isEqualTo(2)

    seriesDao.deleteAll()
    assertThat(seriesDao.count()).isEqualTo(0)
  }

  @Test
  fun `it should find and return all series`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )
    val series2 = Series(
      name = "Series2",
      description = "Series2 description",
      libraryId = library.id,
    )

    seriesDao.insert(series)
    seriesDao.insert(series2)

    val all = seriesDao.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.map(Series::name))
      .containsExactlyInAnyOrder("Series", "Series2")
  }

  @Test
  fun `it should find and return all series by its id's`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )
    val series2 = Series(
      name = "Series2",
      description = "Series2 description",
      libraryId = library.id,
    )

    seriesDao.insert(series)
    seriesDao.insert(series2)

    val all = seriesDao.findAllByIds(listOf(series.id, series2.id))
    assertThat(all).hasSize(2)
    assertThat(all.map(Series::name))
      .containsExactlyInAnyOrder("Series", "Series2")
  }

  @Test
  fun `it should find by id and return a series`() {
    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )

    seriesDao.insert(series)

    val found = seriesDao.findByIdOrNull(series.id)
    assertThat(found).isNotNull
    assertThat(found?.name).isEqualTo("Series")
  }

  @Test
  fun `it should return null when finding a non-existent series`() {
    val found = seriesDao.findByIdOrNull("12345")
    assertThat(found).isNull()
  }

  @Test
  fun `it should return only the series from a library`() {
    val library2 = makeLibrary("Library2", "", ownerId = user.id)
    libraryRepository.insert(library2)

    val series = Series(
      name = "Series",
      description = "Series description",
      libraryId = library.id,
    )
    val series2 = Series(
      name = "Series2",
      description = "Series2 description",
      libraryId = library2.id,
    )

    seriesDao.insert(series)
    seriesDao.insert(series2)

    val all = seriesDao.findByLibraryId(library.id)
    assertThat(all).hasSize(1)
    assertThat(all.map(Series::name))
      .containsExactlyInAnyOrder("Series")
  }
}
