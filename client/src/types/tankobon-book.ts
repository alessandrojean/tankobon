import { Dimensions } from './tankobon-dimensions'
import { Entity } from './tankobon-entity'
import { MonetaryAmount } from './tankobon-monetary'

export type BookEntity = Entity<BookAttributes> & {
  type: 'BOOK'
}

export interface BookAttributes {
  arrivedAt?: string,
  barcode?: string,
  billedAt?: string,
  boughtAt?: string,
  code: string,
  createdAt: string,
  dimensions: Dimensions,
  isInLibrary: boolean,
  labelPrice: MonetaryAmount,
  modifiedAt: string,
  notes: string,
  number: string,
  pageCount: number,
  paidPrice: MonetaryAmount,
  synopsis: string,
  title: string
}

export interface BookContributor {
  name: string,
  role: string,
}

export type BookIncludes = 'contributor' | 'collection' | 'publisher'
  | 'series' | 'store' | 'tag' | 'library' | 'cover_art'
export type BookSort = 'title' | 'createdAt' | 'modifiedAt' |
  'boughtAt' | 'billedAt' | 'arrivedAt' | 'number' | 'pageCount'
