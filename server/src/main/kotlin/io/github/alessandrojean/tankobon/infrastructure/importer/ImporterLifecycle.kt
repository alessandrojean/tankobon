package io.github.alessandrojean.tankobon.infrastructure.importer

import io.github.alessandrojean.tankobon.domain.model.ContributorRole
import io.github.alessandrojean.tankobon.domain.model.DuplicateCodeException
import io.github.alessandrojean.tankobon.domain.model.IdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.LengthUnit
import io.github.alessandrojean.tankobon.domain.model.MassUnit
import io.github.alessandrojean.tankobon.domain.model.Person
import io.github.alessandrojean.tankobon.domain.model.Publisher
import io.github.alessandrojean.tankobon.domain.model.RelationIsNotFromSameLibraryException
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.domain.persistence.CollectionRepository
import io.github.alessandrojean.tankobon.domain.persistence.ContributorRoleRepository
import io.github.alessandrojean.tankobon.domain.persistence.PersonRepository
import io.github.alessandrojean.tankobon.domain.persistence.PublisherRepository
import io.github.alessandrojean.tankobon.domain.service.BookLifecycle
import io.github.alessandrojean.tankobon.domain.service.ContributorRoleLifecycle
import io.github.alessandrojean.tankobon.domain.service.PersonLifecycle
import io.github.alessandrojean.tankobon.domain.service.PublisherLifecycle
import io.github.alessandrojean.tankobon.infrastructure.image.BookCoverLifecycle
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookContributorCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.DimensionsDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.WeightDto
import mu.KotlinLogging
import org.javamoney.moneta.FastMoney
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Component
class ImporterLifecycle(
  private val publisherRepository: PublisherRepository,
  private val publisherLifecycle: PublisherLifecycle,
  private val personRepository: PersonRepository,
  private val personLifecycle: PersonLifecycle,
  private val contributorRoleRepository: ContributorRoleRepository,
  private val contributorRoleLifecycle: ContributorRoleLifecycle,
  private val collectionRepository: CollectionRepository,
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val bookLifecycle: BookLifecycle,
  private val bookCoverLifecycle: BookCoverLifecycle,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(DuplicateCodeException::class, RelationIsNotFromSameLibraryException::class)
  suspend fun importToCollection(
    collectionId: String,
    import: ImporterBookResult,
    user: TankobonUser,
  ): BookEntityDto {
    logger.info { "Importing book ${import.id} from ${import.provider} to collection $collectionId" }

    val libraryId = collectionRepository.findByIdOrNull(collectionId)?.libraryId
      ?: throw IdDoesNotExistException("Collection not found")

    if (bookRepository.existsByCodeInLibrary(import.isbn, libraryId)) {
      throw DuplicateCodeException("A book with the code ${import.isbn} already exists")
    }

    val existingPublisher = publisherRepository.findByNameInLibraryOrNull(import.publisher, libraryId)
    val existingPeople = personRepository
      .findByNamesInLibraryOrNull(
        names = import.contributors.map { it.name }.distinct(),
        libraryId = libraryId,
      )
      .associateBy { it.name.lowercase() }
      .toMutableMap()
    val existingRoles = contributorRoleRepository
      .findByNamesInLibraryOrNull(
        names = import.contributors.map { it.role }.distinct(),
        libraryId = libraryId,
      )
      .associateBy { it.name.lowercase() }
      .toMutableMap()

    val created = transactionTemplate.execute {
      val publisher = existingPublisher
        ?: publisherLifecycle.addPublisher(Publisher(import.publisher, libraryId = libraryId))

      val contributors = import.contributors.map {
        val person = existingPeople[it.name.lowercase()]
          ?: personLifecycle.addPerson(Person(it.name, libraryId = libraryId))
            .also { person -> existingPeople[it.name.lowercase()] = person }
        val role = existingRoles[it.role.lowercase()]
          ?: contributorRoleLifecycle.addContributorRole(ContributorRole(it.role, libraryId = libraryId))
            .also { role -> existingRoles[it.role.lowercase()] = role }

        BookContributorCreationDto(person.id, role.id)
      }

      val book = BookCreationDto(
        collection = collectionId,
        contributors = contributors,
        publishers = listOf(publisher.id),
        code = import.isbn,
        barcode = import.isbn,
        title = import.title,
        subtitle = import.subtitle,
        number = import.number,
        labelPrice = import.labelPrice ?: FastMoney.of(0, "USD"),
        paidPrice = import.labelPrice ?: FastMoney.of(0, "USD"),
        dimensions = DimensionsDto(
          width = import.dimensions?.width ?: 0f,
          height = import.dimensions?.height ?: 0f,
          depth = import.dimensions?.depth ?: 0f,
          unit = import.dimensions?.unit ?: LengthUnit.CENTIMETER,
        ),
        isInLibrary = true,
        pageCount = import.pageCount,
        synopsis = import.synopsis,
        weight = WeightDto(
          value = import.weight?.value ?: 0f,
          unit = import.weight?.unit ?: MassUnit.KILOGRAM,
        ),
        notes = "",
        links = import.links,
      )

      val created = bookLifecycle.addBook(book, user)
      val bookDomain = bookRepository.findByIdOrNull(created.id)!!

      bookLifecycle.updateBook(bookDomain.copy(source = import.provider, sourceBookId = import.id))

      created
    }

    if (!import.coverUrl.isNullOrEmpty()) {
      bookCoverLifecycle.downloadCover(created!!.id, import.coverUrl)
    }

    // Get from the repository again to also check and get the cover art.
    return bookDtoRepository.findByIdOrNull(created!!.id)!!
  }
}
