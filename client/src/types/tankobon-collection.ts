import { Entity } from './tankobon-entity'

export type CollectionEntity = Entity<CollectionAttributes> & {
  type: 'COLLECTION'
}

export interface CollectionAttributes {
  name: string,
  description: string,
}

export interface CollectionCreation {
  name: string,
  description: string,
  library: string,
}

export interface CollectionUpdate {
  id: string,
  name: string,
  description: string,
}

export type CollectionSort = 'name'
export type CollectionIncludes = 'library'
