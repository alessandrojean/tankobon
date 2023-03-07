package io.github.alessandrojean.tankobon.domain.model

open class CodedException : Exception {
  val code: String

  constructor(cause: Throwable, code: String) : super(cause) {
    this.code = code
  }

  constructor(message: String, code: String) : super(message) {
    this.code = code
  }
}

fun Exception.withCode(code: String) = CodedException(this, code)

class UserEmailAlreadyExistsException(message: String, code: String = "") : CodedException(message, code)
class DuplicateNameException(message: String, code: String = "") : CodedException(message, code)
class DuplicateCodeException(message: String, code: String = "") : CodedException(message, code)
class RelationIdDoesNotExistException(message: String, code: String = "") : CodedException(message, code)
class UserDoesNotHaveAccessException(message: String, code: String = "") : CodedException(message, code)
class RelationIsNotFromSameLibraryException(message: String, code: String = "") : CodedException(message, code)
class BookLibraryChangedException(message: String, code: String = "") : CodedException(message, code)
