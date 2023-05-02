import type { Entity } from './tankobon-entity'

export type LibraryEntity = Entity<LibraryAttributes> & {
  type: 'LIBRARY'
}

export interface LibraryAttributes {
  name: string
  description: string
}

export interface LibraryCreation {
  name: string
  description: string
  owner?: string
}

export interface LibraryUpdate {
  id: string
  name: string
  description: string
  owner?: string
  sharedUsers: string[]
}

export type LibraryIncludes = 'owner'
