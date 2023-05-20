import type { Entity } from './tankobon-entity'

export type PublisherEntity = Entity<PublisherAttributes> & {
  type: 'PUBLISHER'
}

export interface PublisherAttributes {
  name: string
  description: string
  links: PublisherLinks
}

export interface PublisherLinks {
  website: string | null
  store: string | null
  twitter: string | null
  instagram: string | null
  facebook: string | null
  youTube: string | null
}

export type PublisherLinkType = keyof PublisherLinks

export interface PublisherCreation {
  name: string
  description: string
  links: PublisherLinks
  library: string
}

export interface PublisherUpdate {
  id: string
  name: string
  description: string
  links: PublisherLinks
}

export type PublisherSort = 'name' | 'createdAt' | 'modifiedAt'
export type PublisherIncludes = 'library' | 'publisher_picture'
