import type { Entity } from './tankobon-entity'

export type ContributorEntity = Entity<ContributorAttributes> & {
  type: 'CONTRIBUTOR'
}

export interface ContributorAttributes {
  role: {
    id: string
    name: string
  }
  person: {
    id: string
    name: string
  }
}

export type ContributorIncludes = 'person_picture'
