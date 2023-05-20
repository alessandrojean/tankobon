import type { Entity } from './tankobon-entity'

export type StoreEntity = Entity<StoreAttributes> & {
  type: 'STORE'
}

export interface StoreAttributes {
  name: string
  description: string
  links: StoreLinks
}

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
  library: string
}

export interface StoreUpdate {
  id: string
  name: string
  description: string
  links: StoreLinks
}

export type StoreSort = 'name' | 'createdAt' | 'modifiedAt'
export type StoreIncludes = 'library'
