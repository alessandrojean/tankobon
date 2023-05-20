import type { Entity } from './tankobon-entity'

export type PersonEntity = Entity<PersonAttributes> & {
  type: 'PERSON'
}

export interface PersonAttributes {
  name: string
  description: string
  links: PersonLinks
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
  library: string
}

export interface PersonUpdate {
  id: string
  name: string
  description: string
  links: PersonLinks
}

export type PersonSort = 'name' | 'createdAt' | 'modifiedAt'
export type PersonIncludes = 'library' | 'person_picture'
