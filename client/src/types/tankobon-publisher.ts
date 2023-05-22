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
  foundingYear: number | null
  dissolutionYear: number | null
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
  foundingYear: number | null
  dissolutionYear: number | null
  library: string
}

export interface PublisherUpdate {
  id: string
  name: string
  description: string
  links: PublisherLinks
  legalName: string
  location: string | null
  foundingYear: number | null
  dissolutionYear: number | null
}

export type PublisherSort = 'name' | 'createdAt' | 'modifiedAt' | 'legalName' | 'location' | 'foundingYear' | 'dissolutionYear'
export type PublisherIncludes = 'library' | 'publisher_picture'
