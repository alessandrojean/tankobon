import type { Entity } from './tankobon-entity'
import type { CollectionResponse } from './tankobon-response'

export type PreferenceEntity = Entity<PreferenceAttributes> & {
  type: 'PREFERENCE'
}

export interface PreferenceAttributes {
  key: string,
  value: string,
}

export type Preferences = Record<string, string>

export type PreferenceCollectionResponse = CollectionResponse<PreferenceEntity>
