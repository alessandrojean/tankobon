import type { TankobonEntity } from './tankobon-entity'
import type { TankobonSuccessCollectionResponse } from './tankobon-response'

export type TankobonPreferenceEntity = TankobonEntity<TankobonPreferenceAttributes> & {
  type: 'PREFERENCE'
}

export interface TankobonPreferenceAttributes {
  key: string,
  value: string,
}

export type Preferences = Record<string, string>

export type TankobonPreferenceCollectionResponse = TankobonSuccessCollectionResponse<TankobonPreferenceEntity>
