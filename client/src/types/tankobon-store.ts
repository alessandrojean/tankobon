import { Entity } from './tankobon-entity'

export type StoreEntity = Entity<StoreAttributes> & {
  type: 'STORE'
}

export interface StoreAttributes {
  name: string,
  description: string,
}

export interface StoreCreation {
  name: string,
  description: string,
  library: string,
}

export interface StoreUpdate {
  id: string,
  name: string,
  description: string,
}

export type StoreSort = 'name' | 'createdAt' | 'modifiedAt'
export type StoreIncludes = 'library'
