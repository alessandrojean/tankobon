import type { Entity } from './tankobon-entity'

export type PublisherEntity = Entity<PublisherAttributes> & {
  type: 'PUBLISHER'
}

export interface PublisherAttributes {
  name: string
  description: string
  links: PublisherLinks
  legalName: string
  location: string | null
  createdAt: string
  modifiedAt: string
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
  legalName: string
  location: string | null
  library: string
}

export interface PublisherUpdate {
  id: string
  name: string
  description: string
  links: PublisherLinks
  legalName: string
  location: string | null
}

export type PublisherSort = 'name' | 'createdAt' | 'modifiedAt' | 'legalName' | 'location'
export type PublisherIncludes = 'library' | 'publisher_picture'
