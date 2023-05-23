import type { Entity } from './tankobon-entity'

export type PersonEntity = Entity<PersonAttributes> & {
  type: 'PERSON'
}

export interface PersonAttributes {
  name: string
  description: string
  links: PersonLinks
  bornAt: string | null
  diedAt: string | null
  nationality: string | null
  nativeName: PersonNativeName
  createdAt: string
  modifiedAt: string
}

export interface PersonNativeName {
  name: string
  language: string | null
}

export interface PersonLinks {
  website: string | null
  twitter: string | null
  instagram: string | null
  facebook: string | null
  pixiv: string | null
  deviantArt: string | null
  youTube: string | null
}

export type PersonLinkType = keyof PersonLinks

export interface PersonCreation {
  name: string
  description: string
  links: PersonLinks
  bornAt: string | null
  diedAt: string | null
  nationality: string | null
  nativeName: PersonNativeName
  library: string
}

export interface PersonUpdate {
  id: string
  name: string
  description: string
  links: PersonLinks
  bornAt: string | null
  diedAt: string | null
  nationality: string | null
  nativeName: PersonNativeName
}

export type PersonSort = 'name' | 'createdAt' | 'modifiedAt' | 'nativeName' | 'bornAt' | 'diedAt'
export type PersonIncludes = 'library' | 'person_picture'
