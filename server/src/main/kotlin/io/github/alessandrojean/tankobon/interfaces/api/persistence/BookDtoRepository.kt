package io.github.alessandrojean.tankobon.interfaces.api.persistence

import io.github.alessandrojean.tankobon.domain.model.BookSearch
import io.github.alessandrojean.tankobon.domain.model.TankobonUser
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookCreationDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookEntityDto
import io.github.alessandrojean.tankobon.interfaces.api.rest.dto.BookUpdateDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookDtoRepository {
  fun findByIdOrNull(bookId: String): BookEntityDto?
  fun findAll(search: BookSearch, pageable: Pageable): Page<BookEntityDto>
  fun findAllByIsbnInLibraries(isbn: String, librariesIds: Collection<String>): Collection<BookEntityDto>

  fun insert(book: BookCreationDto, user: TankobonUser): String
  fun update(bookId: String, book: BookUpdateDto, user: TankobonUser)
}
