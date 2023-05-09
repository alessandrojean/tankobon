import type { BookAttributes, BookEntity, BookIncludes } from '@/types/tankobon-book'
import type { CollectionAttributes } from '@/types/tankobon-collection'
import type { ContributorAttributes, ContributorEntity, ContributorIncludes } from '@/types/tankobon-contributor'
import type { Entity, Relationship } from '@/types/tankobon-entity'
import type { ExternalBookEntity, ExternalBookIncludes } from '@/types/tankobon-external-book'
import type { ImageDetailsAttributes } from '@/types/tankobon-image-details'
import type { ImporterSourceAttributes } from '@/types/tankobon-importer-source'
import type { LibraryAttributes, LibraryEntity, LibraryIncludes } from '@/types/tankobon-library'
import type { PersonEntity, PersonIncludes } from '@/types/tankobon-person'
import type { PublisherAttributes, PublisherEntity, PublisherIncludes } from '@/types/tankobon-publisher'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { SeriesAttributes } from '@/types/tankobon-series'
import type { StoreAttributes } from '@/types/tankobon-store'
import type { TagAttributes } from '@/types/tankobon-tag'
import type { UserAttributes, UserEntity, UserIncludes } from '@/types/tankobon-user'

/* eslint-disable @typescript-eslint/indent */
type AttributeType<E extends object, T extends EntityAttributes<E>> =
  T extends 'LIBRARY' ? LibraryAttributes :
  T extends 'LIBRARY_SHARING' ? LibraryAttributes :
  T extends 'AVATAR' ? ImageDetailsAttributes :
  T extends 'COVER_ART' ? ImageDetailsAttributes :
  T extends 'PERSON_PICTURE' ? ImageDetailsAttributes :
  T extends 'PUBLISHER_PICTURE' ? ImageDetailsAttributes :
  T extends 'USER' ? UserAttributes :
  T extends 'OWNER' ? UserAttributes :
  T extends 'IMPORTER_SOURCE' ? ImporterSourceAttributes :
  T extends 'SERIES' ? SeriesAttributes :
  T extends 'CONTRIBUTOR' ? ContributorAttributes :
  T extends 'PUBLISHER' ? PublisherAttributes :
  T extends 'COLLECTION' ? CollectionAttributes :
  T extends 'STORE' ? StoreAttributes :
  T extends 'CONTRIBUTOR' ? ContributorAttributes :
  T extends 'TAG' ? TagAttributes :
  T extends 'PREVIOUS_BOOK' ? BookAttributes :
  T extends 'NEXT_BOOK' ? BookAttributes :
  unknown

type EntityAttributes<T> =
  T extends LibraryEntity ? Uppercase<LibraryIncludes> :
  T extends UserEntity ? Uppercase<UserIncludes> :
  T extends ExternalBookEntity ? Uppercase<ExternalBookIncludes> :
  T extends BookEntity ? Uppercase<BookIncludes> :
  T extends PersonEntity ? Uppercase<PersonIncludes> :
  T extends ContributorEntity ? Uppercase<ContributorIncludes> :
  T extends PublisherEntity ? Uppercase<PublisherIncludes> :
  unknown
/* eslint-enable */

export function getRelationship<A extends object, E extends Entity<A>, T extends EntityAttributes<E>>(
  entity: E | undefined | null,
  type: T,
): Relationship<AttributeType<E, T>> | undefined {
  return entity?.relationships?.find(r => r.type === type)
}

export function getRelationships<A extends object, E extends Entity<A>, T extends EntityAttributes<E>>(
  entity: E | undefined | null,
  type: T,
): Relationship<AttributeType<E, T>>[] | undefined {
  return entity?.relationships?.filter(r => r.type === type)
}

export function createEmptyPaginatedResponse<T>(): PaginatedResponse<T> {
  return {
    result: 'OK',
    response: 'COLLECTION',
    data: [],
    pagination: {
      currentPage: 1,
      totalElements: 20,
      totalPages: 1,
    },
  }
}

export const ACCEPTED_IMAGE_FORMATS = [
  'image/png',
  'image/jpeg',
  'image/bmp',
  'image/gif',
  'image/x-webp',
  'image/webp',
]
