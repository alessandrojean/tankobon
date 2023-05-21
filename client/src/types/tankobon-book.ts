import type { Dimensions } from './tankobon-dimensions'
import type { Entity } from './tankobon-entity'
import type { MonetaryAmount } from './tankobon-monetary'

export type BookEntity = Entity<BookAttributes> & {
  type: 'BOOK'
}

export interface BookAttributes {
  arrivedAt?: string
  barcode?: string
  billedAt?: string
  boughtAt?: string
  code: BookCode
  createdAt: string
  dimensions: Dimensions
  isInLibrary: boolean
  labelPrice: MonetaryAmount
  modifiedAt: string
  notes: string
  number: string
  pageCount: number
  paidPrice: MonetaryAmount
  subtitle: string
  synopsis: string
  title: string
  weightKg: number
  links: BookLinks
}

export interface BookContributor {
  name: string
  role: string
}

export type BookCodeType = 'ISBN_13' | 'ISBN_10' | 'ISSN' | 'EAN_13' | 'UNKNOWN'

export interface BookCode {
  type: BookCodeType
  code: string
}

export type BookCodeIsbn = { type: 'ISBN_13' | 'ISBN_10' } & BookCode & {
  group: number | null
  region: string | null
  language: string | null
}

export function isIsbnCode(code: BookCode | BookCodeIsbn | undefined): code is BookCodeIsbn {
  return code?.type === 'ISBN_10' || code?.type === 'ISBN_13'
}

export interface BookLinks {
  amazon: string | null
  openLibrary: string | null
  skoob: string | null
  goodreads: string | null
  guiaDosQuadrinhos: string | null
}

export type BookLinkType = keyof BookLinks

export type BookIncludes = 'contributor' | 'collection' | 'publisher'
| 'series' | 'store' | 'tag' | 'library' | 'cover_art'
| 'previous_book' | 'next_book'
export type BookSort = 'title' | 'createdAt' | 'modifiedAt' |
'boughtAt' | 'billedAt' | 'arrivedAt' | 'number' | 'pageCount' | 'weightKg'

export interface BookUpdate {
  id: string
  arrivedAt?: string | null
  barcode?: string | null
  billedAt?: string | null
  boughtAt?: string | null
  code: string
  collection: string
  contributors: BookContributorCreateUpdate[]
  dimensions: Dimensions
  isInLibrary: boolean
  labelPrice: MonetaryAmount
  notes: string
  number: string
  pageCount: number
  paidPrice: MonetaryAmount
  publishers: string[]
  series?: string | null
  store?: string | null
  subtitle: string
  synopsis: string
  tags?: string[] | null
  title: string
  weightKg: number
  links: BookLinks
}

export interface BookContributorCreateUpdate {
  person: string
  role: string
}
