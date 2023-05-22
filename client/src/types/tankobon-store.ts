import type { Entity } from './tankobon-entity'

export type StoreEntity = Entity<StoreAttributes> & {
  type: 'STORE'
}

export interface StoreAttributes {
  name: string
  description: string
  links: StoreLinks
  legalName: string
  location: string | null
  type: StoreType | null
  createdAt: string
  modifiedAt: string
}

export type StoreType = 'COMIC_SHOP' | 'BOOKSTORE' | 'NEWSSTAND'

export interface StoreLinks {
  website: string | null
  twitter: string | null
  instagram: string | null
  facebook: string | null
  youTube: string | null
}

export type StoreLinkType = keyof StoreLinks

export interface StoreCreation {
  name: string
  description: string
  links: StoreLinks
  legalName: string
  location: string | null
  type: StoreType | null
  library: string
}

export interface StoreUpdate {
  id: string
  name: string
  description: string
  links: StoreLinks
  legalName: string
  location: string | null
  type: StoreType | null
}

export type StoreSort = 'name' | 'createdAt' | 'modifiedAt' | 'legalName' | 'location'
export type StoreIncludes = 'library' | 'store_picture'
