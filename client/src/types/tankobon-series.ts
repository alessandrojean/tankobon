import type { AlternativeName } from './tankobon-alternative-name'
import type { Entity } from './tankobon-entity'

export type SeriesEntity = Entity<SeriesAttributes> & {
  type: 'SERIES'
}

export interface SeriesAttributes {
  name: string
  description: string
  type: SeriesType
  alternativeNames: AlternativeName[]
}

export type SeriesType = 'MANGA' | 'MANHWA' | 'MANHUA' | 'COMIC'
| 'BOOK' | 'NOVEL' | 'DATABOOK' | 'ARTBOOK' | 'LIGHT_NOVEL'

export interface SeriesCreation {
  name: string
  description: string
  type: SeriesType | null
  alternativeNames: AlternativeName[]
  library: string
}

export interface SeriesUpdate extends Omit<SeriesCreation, 'library'> {
  id: string
}

export type SeriesSort = 'name' | 'createdAt' | 'modifiedAt'
export type SeriesIncludes = 'library' | 'series_cover'
