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
class UserDoesNotHaveAccessException(message: String = "The user does not have access", code: String = "") : CodedException(message, code)
class RelationIsNotFromSameLibraryException(message: String, code: String = "") : CodedException(message, code)
class BookLibraryChangedException(message: String, code: String = "") : CodedException(message, code)
class IdDoesNotExistException(message: String, code: String = "") : CodedException(message, code)
class CantChangePasswordInDemoModeException(message: String = "Changing the password is not allowed in demo mode", code: String = "") : CodedException(message, code)
class CantChangeDemoUserAttributesException(message: String = "Changing the demo user attributes is not allowed", code: String = "") : CodedException(message, code)
class LibraryOwnerChangedException(message: String = "Only administrators can change a library owner", code: String = "") : CodedException(message, code)
class ServerAlreadyClaimedException(message: String = "This server has already been claimed", code: String = "") : CodedException(message, code)
class FinishedDateIsBeforeStartedDateException(message: String = "The finished date is before the started date", code: String = "") : CodedException(message, code)
