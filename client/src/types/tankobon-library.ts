import { Entity } from './tankobon-entity'

export type LibraryEntity = Entity<LibraryAttributes> & {
  type: 'LIBRARY'
}

export interface LibraryAttributes {
  name: string,
  description: string,
}

export interface AddOneLibrary {
  name: string,
  description: string,
  owner?: string,
}
