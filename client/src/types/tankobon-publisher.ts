import type { Entity } from './tankobon-entity'

export type PublisherEntity = Entity<PublisherAttributes> & {
  type: 'PUBLISHER'
}

export interface PublisherAttributes {
  name: string
  description: string
}

export interface PublisherCreation {
  name: string
  description: string
  library: string
}

export interface PublisherUpdate {
  id: string
  name: string
  description: string
}

export type PublisherSort = 'name' | 'createdAt' | 'modifiedAt'
export type PublisherIncludes = 'library' | 'publisher_picture'
