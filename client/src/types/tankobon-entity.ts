export interface Entity<Attributes> {
  id: string
  type: EntityType
  attributes: Attributes
  relationships: Relationship<any>[] | undefined
}

export type EntityType = 'USER' | 'LIBRARY' | 'PREFERENCE'
| 'AUTHENTICATION_ACTIVITY' | 'COLLECTION' | 'SERIES' | 'PUBLISHER'
| 'TAG' | 'STORE' | 'CONTRIBUTOR_ROLE' | 'IMPORTER_SOURCE'
| 'EXTERNAL_BOOK' | 'BOOK' | 'CONTRIBUTOR' | 'PERSON' | 'READ_PROGRESS'

export interface Relationship<Attributes> {
  id: string
  type: RelationshipType
  attributes: Attributes | undefined
}

export type RelationshipType = 'AVATAR' | 'OWNER' | 'USER' | 'LIBRARY_SHARING'
| 'LIBRARY' | 'IMPORTER_SOURCE' | 'COVER_ART' | 'SERIES' | 'CONTRIBUTOR'
| 'PREVIOUS_BOOK' | 'NEXT_BOOK'

export type Includes = Lowercase<RelationshipType>[]
