package io.github.alessandrojean.tankobon.domain.service

import io.github.alessandrojean.tankobon.application.events.EventPublisher
import io.github.alessandrojean.tankobon.domain.model.Book
import io.github.alessandrojean.tankobon.domain.model.BookLibraryChangedException
import io.github.alessandrojean.tankobon.domain.model.DomainEvent
import io.github.alessandrojean.tankobon.domain.model.DuplicateCodeException
import io.github.alessandrojean.tankobon.domain.model.RelationIdDoesNotExistException
import io.github.alessandrojean.tankobon.domain.model.RelationIsNotFromSameLibraryException
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.domain.model.UserDoesNotHaveAccessException
import io.github.alessandrojean.tankobon.domain.persistence.BookRepository
import io.github.alessandrojean.tankobon.interfaces.api.persistence.BookDtoRepository
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookUpdateDto
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

private val logger = KotlinLogging.logger {}

@Service
class BookLifecycle(
  private val bookRepository: BookRepository,
  private val bookDtoRepository: BookDtoRepository,
  private val eventPublisher: EventPublisher,
  private val transactionTemplate: TransactionTemplate,
) {

  @Throws(
    RelationIdDoesNotExistException::class,
    RelationIsNotFromSameLibraryException::class,
    UserDoesNotHaveAccessException::class,
    DuplicateCodeException::class,
  )
  fun addBook(book: BookCreationDto, user: TankobonUser): BookEntityDto {
    logger.info { "Adding new book: $book" }

    if (bookRepository.findByCodeOrNull(book.code) != null) {
      throw DuplicateCodeException("A book with the code ${book.code} already exists")
    }

    val bookId = bookDtoRepository.insert(book, user)
    val bookDomain = bookRepository.findByIdOrNull(bookId)!!
    eventPublisher.publishEvent(DomainEvent.BookAdded(bookDomain))

    return bookDtoRepository.findByIdOrNull(bookId)!!
  }

  @Throws(
    RelationIdDoesNotExistException::class,
    RelationIsNotFromSameLibraryException::class,
    UserDoesNotHaveAccessException::class,
    DuplicateCodeException::class,
  )
  fun addBook(book: Book): Book {
    logger.info { "Adding new book: $book" }

    if (bookRepository.findByCodeOrNull(book.code) != null) {
      throw DuplicateCodeException("A book with the code ${book.code} already exists")
    }

    bookRepository.insert(book)
    eventPublisher.publishEvent(DomainEvent.BookAdded(book))

    return bookRepository.findByIdOrNull(book.id)!!
  }

  @Throws(
    RelationIdDoesNotExistException::class,
    RelationIsNotFromSameLibraryException::class,
    UserDoesNotHaveAccessException::class,
    DuplicateCodeException::class,
    BookLibraryChangedException::class,
  )
  fun updateBook(toUpdate: Book) {
    logger.info { "Updating book: $toUpdate" }

    if (bookRepository.findByIdOrNull(toUpdate.id) == null) {
      throw IllegalArgumentException("Cannot update book that does not exist")
    }

    if (bookRepository.findByCodeOrNull(toUpdate.code)?.id != toUpdate.id) {
      throw DuplicateCodeException("A book with the code ${toUpdate.code} already exists")
    }

    bookRepository.update(toUpdate)
    eventPublisher.publishEvent(DomainEvent.BookUpdated(toUpdate))
  }

  @Throws(
    RelationIdDoesNotExistException::class,
    RelationIsNotFromSameLibraryException::class,
    UserDoesNotHaveAccessException::class,
    DuplicateCodeException::class,
    BookLibraryChangedException::class,
  )
  fun updateBook(bookId: String, toUpdate: BookUpdateDto, user: TankobonUser) {
    logger.info { "Updating book: $toUpdate" }

    if (bookRepository.findByIdOrNull(bookId) == null) {
      throw IllegalArgumentException("Cannot update book that does not exist")
    }

    if (bookRepository.findByCodeOrNull(toUpdate.code)?.id != bookId) {
      throw DuplicateCodeException("A book with the code ${toUpdate.code} already exists")
    }

    bookDtoRepository.update(bookId, toUpdate, user)

    val bookDomain = bookRepository.findByIdOrNull(bookId)!!
    eventPublisher.publishEvent(DomainEvent.BookUpdated(bookDomain))
  }

  fun deleteBook(book: Book) {
    transactionTemplate.executeWithoutResult {
      bookRepository.delete(book.id)
    }

    eventPublisher.publishEvent(DomainEvent.BookDeleted(book))
  }
}