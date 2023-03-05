package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

sealed class DomainEvent : Serializable {
  data class UserUpdated(val user: TankobonUser) : DomainEvent()
  data class UserDeleted(val user: TankobonUser) : DomainEvent()

  data class LibraryAdded(val library: Library) : DomainEvent()
  data class LibraryUpdated(val library: Library) : DomainEvent()
  data class LibraryDeleted(val library: Library) : DomainEvent()

  data class CollectionAdded(val collection: Collection) : DomainEvent()
  data class CollectionUpdated(val collection: Collection) : DomainEvent()
  data class CollectionDeleted(val collection: Collection) : DomainEvent()

  data class SeriesAdded(val series: Series) : DomainEvent()
  data class SeriesUpdated(val series: Series) : DomainEvent()
  data class SeriesDeleted(val series: Series) : DomainEvent()

  data class PublisherAdded(val publisher: Publisher) : DomainEvent()
  data class PublisherUpdated(val publisher: Publisher) : DomainEvent()
  data class PublisherDeleted(val publisher: Publisher) : DomainEvent()
}
