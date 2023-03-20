import { Entity } from './tankobon-entity'

export type PersonEntity = Entity<PersonAttributes> & {
  type: 'COLLECTION'
}

export interface PersonAttributes {
  name: string,
  description: string,
}

export interface PersonCreation {
  name: string,
  description: string,
  library: string,
}

export interface PersonUpdate {
  id: string,
  name: string,
  description: string,
}

export type PersonSort = 'name' | 'createdAt' | 'modifiedAt'
export type PersonIncludes = 'library'
