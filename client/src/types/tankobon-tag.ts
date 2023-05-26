import type { Entity } from './tankobon-entity'

export type TagEntity = Entity<TagAttributes> & {
  type: 'TAG'
}

export interface TagAttributes {
  name: string
  description: string
  createdAt: string
  modifiedAt: string
}

export interface TagCreation {
  name: string
  description: string
  library: string
}

export interface TagUpdate {
  id: string
  name: string
  description: string
}

export type TagSort = 'name' | 'createdAt' | 'modifiedAt'
export type TagIncludes = 'library'
