import type { BookEntity } from './tankobon-book'
import type { CollectionEntity } from './tankobon-collection'
import type { PersonEntity } from './tankobon-person'
import type { PublisherEntity } from './tankobon-publisher'
import type { SeriesEntity } from './tankobon-series'
import type { StoreEntity } from './tankobon-store'
import type { TagEntity } from './tankobon-tag'

export interface SearchObject {
  books: BookEntity[]
  publishers: PublisherEntity[]
  stores: StoreEntity[]
  series: SeriesEntity[]
  people: PersonEntity[]
  tags: TagEntity[]
  collections: CollectionEntity[]
}
