export interface TankobonEntity<Attributes> {
  id: string,
  type: TankobonEntityType,
  attributes: Attributes,
  relationships: TankobonRelationship<any>[] | undefined,
}

export type TankobonEntityType = 'USER' | 'LIBRARY'

export interface TankobonRelationship<Attributes> {
  id: string,
  type: TankobonRelationshipType,
  attributes: Attributes | undefined,
}

export type TankobonRelationshipType = 'AVATAR' | 'OWNER'

export type Includes = TankobonRelationshipType[]

