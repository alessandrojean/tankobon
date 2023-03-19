import { Entity } from './tankobon-entity'

export type SeriesEntity = Entity<SeriesAttributes> & {
  type: 'SERIES'
}

export interface SeriesAttributes {
  name: string,
  description: string,
}

export interface SeriesCreation {
  name: string,
  description: string,
  library: string,
}

export interface SeriesUpdate {
  id: string,
  name: string,
  description: string,
}

export type SeriesSort = 'name' | 'createdAt' | 'modifiedAt'
export type SeriesIncludes = 'library'
