package io.github.alessandrojean.tankobon.domain.model

import java.io.Serializable

sealed class DomainEvent : Serializable {
  data class UserAdded(val user: TankobonUser) : DomainEvent()
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

  data class StoreAdded(val store: Store) : DomainEvent()
  data class StoreUpdated(val store: Store) : DomainEvent()
  data class StoreDeleted(val store: Store) : DomainEvent()

  data class ContributorRoleAdded(val contributorRole: ContributorRole) : DomainEvent()
  data class ContributorRoleUpdated(val contributorRole: ContributorRole) : DomainEvent()
  data class ContributorRoleDeleted(val contributorRole: ContributorRole) : DomainEvent()

  data class PersonAdded(val person: Person) : DomainEvent()
  data class PersonUpdated(val person: Person) : DomainEvent()
  data class PersonDeleted(val person: Person) : DomainEvent()

  data class TagAdded(val tag: Tag) : DomainEvent()
  data class TagUpdated(val tag: Tag) : DomainEvent()
  data class TagDeleted(val tag: Tag) : DomainEvent()

  data class BookAdded(val book: Book) : DomainEvent()
  data class BookUpdated(val book: Book) : DomainEvent()
  data class BookDeleted(val book: Book) : DomainEvent()

  data class ReadProgressAdded(val readProgress: ReadProgress) : DomainEvent()
  data class ReadProgressUpdated(val readProgress: ReadProgress) : DomainEvent()
  data class ReadProgressDeleted(val readProgress: ReadProgress) : DomainEvent()

  data class BookCoverAdded(val imageDetails: Image) : DomainEvent()
  data class BookCoverUpdated(val imageDetails: Image) : DomainEvent()
  data class BookCoverDeleted(val imageDetails: Image) : DomainEvent()

  data class UserAvatarAdded(val imageDetails: Image) : DomainEvent()
  data class UserAvatarUpdated(val imageDetails: Image) : DomainEvent()
  data class UserAvatarDeleted(val imageDetails: Image) : DomainEvent()

  data class PersonPictureAdded(val imageDetails: Image) : DomainEvent()
  data class PersonPictureUpdated(val imageDetails: Image) : DomainEvent()
  data class PersonPictureDeleted(val imageDetails: Image) : DomainEvent()

  data class PublisherPictureAdded(val imageDetails: Image) : DomainEvent()
  data class PublisherPictureUpdated(val imageDetails: Image) : DomainEvent()
  data class PublisherPictureDeleted(val imageDetails: Image) : DomainEvent()

  data class SeriesCoverAdded(val imageDetails: Image) : DomainEvent()
  data class SeriesCoverUpdated(val imageDetails: Image) : DomainEvent()
  data class SeriesCoverDeleted(val imageDetails: Image) : DomainEvent()
}
