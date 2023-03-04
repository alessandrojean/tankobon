package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

sealed class DomainEvent : Serializable {
  data class UserUpdated(val user: TankobonUser) : DomainEvent()
  data class UserDeleted(val user: TankobonUser) : DomainEvent()

  data class LibraryAdded(val library: Library) : DomainEvent()
  data class LibraryUpdated(val library: Library) : DomainEvent()
  data class LibraryDeleted(val library: Library) : DomainEvent()
}
