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
  code: BookCode,
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

export type BookCodeType = 'ISBN_13' | 'ISBN_10' | 'ISSN' | 'EAN_13' | 'UNKNOWN'

export interface BookCode {
  type: BookCodeType,
  code: string,
}

export type BookCodeIsbn = { type: 'ISBN_13' | 'ISBN_10' } & BookCode & {
  group: number | null,
  region: string | null,
  language: string | null,
}

export function isIsbnCode(code: BookCode | BookCodeIsbn | undefined): code is BookCodeIsbn {
  return code?.type === 'ISBN_10' || code?.type === 'ISBN_13'
}

export type BookIncludes = 'contributor' | 'collection' | 'publisher'
  | 'series' | 'store' | 'tag' | 'library' | 'cover_art'
export type BookSort = 'title' | 'createdAt' | 'modifiedAt' |
  'boughtAt' | 'billedAt' | 'arrivedAt' | 'number' | 'pageCount'