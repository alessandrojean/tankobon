export interface Entity<Attributes> {
  id: string,
  type: EntityType,
  attributes: Attributes,
  relationships: Relationship<any>[] | undefined,
}

export type EntityType = 'USER' | 'LIBRARY' | 'PREFERENCE'

export interface Relationship<Attributes> {
  id: string,
  type: RelationshipType,
  attributes: Attributes | undefined,
}

export type RelationshipType = 'AVATAR' | 'OWNER' | 'USER'

export type Includes = RelationshipType[]

