import type { AlternativeName } from './tankobon-alternative-name'
import type { Entity } from './tankobon-entity'

export type SeriesEntity = Entity<SeriesAttributes> & {
  type: 'SERIES'
}

export interface SeriesAttributes {
  name: string
  description: string
  type: SeriesType | null
  alternativeNames: AlternativeName[]
  lastNumber: string | null
  originalLanguage: string | null
  links: SeriesLinks
  createdAt: string
  modifiedAt: string
}

export type SeriesType = 'MANGA' | 'MANHWA' | 'MANHUA' | 'COMIC'
| 'BOOK' | 'NOVEL' | 'DATABOOK' | 'ARTBOOK' | 'LIGHT_NOVEL'

export interface SeriesLinks {
  website: string | null
  myAnimeList: string | null
  kitsu: string | null
  aniList: string | null
  mangaUpdates: string | null
  guiaDosQuadrinhos: string | null
  twitter: string | null
  instagram: string | null
}

export type SeriesLinkType = keyof SeriesLinks

export interface SeriesCreation {
  name: string
  description: string
  type: SeriesType | null
  alternativeNames: AlternativeName[]
  lastNumber: string | null
  originalLanguage: string | null
  links: SeriesLinks
  library: string
}

export interface SeriesUpdate extends Omit<SeriesCreation, 'library'> {
  id: string
}

export type SeriesSort = 'name' | 'createdAt' | 'modifiedAt'
export type SeriesIncludes = 'library' | 'series_cover'
