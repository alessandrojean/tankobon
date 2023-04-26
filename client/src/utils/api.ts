import { BookEntity, BookIncludes } from '@/types/tankobon-book'
import { CollectionAttributes } from '@/types/tankobon-collection'
import { ContributorAttributes } from '@/types/tankobon-contributor'
import { Entity, Relationship } from '@/types/tankobon-entity'
import { ExternalBookEntity, ExternalBookIncludes } from '@/types/tankobon-external-book'
import { ImageDetailsAttributes } from '@/types/tankobon-image-details'
import { ImporterSourceAttributes } from '@/types/tankobon-importer-source'
import { LibraryAttributes, LibraryEntity, LibraryIncludes } from '@/types/tankobon-library'
import { PublisherAttributes } from '@/types/tankobon-publisher'
import { SeriesAttributes } from '@/types/tankobon-series'
import { StoreAttributes } from '@/types/tankobon-store'
import { UserAttributes, UserEntity, UserIncludes } from '@/types/tankobon-user'

type AttributeType<E extends object, T extends EntityAttributes<E>> =
  T extends 'LIBRARY' ? LibraryAttributes :
  T extends 'LIBRARY_SHARING' ? LibraryAttributes :
  T extends 'AVATAR' ? ImageDetailsAttributes :
  T extends 'COVER_ART' ? ImageDetailsAttributes :
  T extends 'USER' ? UserAttributes :
  T extends 'OWNER' ? UserAttributes :
  T extends 'IMPORTER_SOURCE' ? ImporterSourceAttributes :
  T extends 'SERIES' ? SeriesAttributes :
  T extends 'CONTRIBUTOR' ? ContributorAttributes :
  T extends 'PUBLISHER' ? PublisherAttributes :
  T extends 'COLLECTION' ? CollectionAttributes :
  T extends 'STORE' ? StoreAttributes :
  unknown

type EntityAttributes<T> =
  T extends LibraryEntity ? Uppercase<LibraryIncludes> :
  T extends UserEntity ? Uppercase<UserIncludes> :
  T extends ExternalBookEntity ? Uppercase<ExternalBookIncludes> :
  T extends BookEntity ? Uppercase<BookIncludes> :
  unknown

export function getRelationship<A extends object, E extends Entity<A>, T extends EntityAttributes<E>>(
  entity: E | undefined | null,
  type: T,
): Relationship<AttributeType<E, T>> | undefined {
  return entity?.relationships?.find((r) => r.type === type)
}

export function getRelationships<A extends object, E extends Entity<A>, T extends EntityAttributes<E>>(
  entity: E | undefined | null,
  type: T,
): Relationship<AttributeType<E, T>>[] | undefined {
  return entity?.relationships?.filter((r) => r.type === type)
}
