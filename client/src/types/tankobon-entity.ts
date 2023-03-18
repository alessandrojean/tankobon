export interface Entity<Attributes> {
  id: string,
  type: EntityType,
  attributes: Attributes,
  relationships: Relationship<any>[] | undefined,
}

export type EntityType = 'USER' | 'LIBRARY' | 'PREFERENCE' 
  | 'AUTHENTICATION_ACTIVITY'

export interface Relationship<Attributes> {
  id: string,
  type: RelationshipType,
  attributes: Attributes | undefined,
}

export type RelationshipType = 'AVATAR' | 'OWNER' | 'USER' | 'LIBRARY_SHARING'

export type Includes = Lowercase<RelationshipType>[]

