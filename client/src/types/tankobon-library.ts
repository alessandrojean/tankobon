import { TankobonEntity } from './tankobon-entity'

export type TankobonLibraryEntity = TankobonEntity<TankobonLibraryAttributes> & {
  type: 'LIBRARY'
}

export interface TankobonLibraryAttributes {
  name: string,
  description: string,
}

export interface AddOneLibrary {
  name: string,
  description: string,
  owner?: string,
}
