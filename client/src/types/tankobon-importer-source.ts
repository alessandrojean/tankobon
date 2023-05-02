import type { Entity } from './tankobon-entity'

export type ImporterSourceEntity = Entity<ImporterSourceAttributes> & {
  type: 'IMPORTER_SOURCE'
}

export interface ImporterSourceAttributes {
  name: string
  description: Record<string, string>
  language: string
  url: string
}

export type ImporterSources = 'cbl' | 'open_library' | 'skoob'

export interface ImportOneBook {
  id: string
  isbn: string
  collection: string
  source: Uppercase<ImporterSources>
}
