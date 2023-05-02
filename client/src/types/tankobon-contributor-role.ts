import type { Entity } from './tankobon-entity'

export type ContributorRoleEntity = Entity<ContributorRoleAttributes> & {
  type: 'CONTRIBUTOR_ROLE'
}

export interface ContributorRoleAttributes {
  name: string
  description: string
}

export interface ContributorRoleCreation {
  name: string
  description: string
  library: string
}

export interface ContributorRoleUpdate {
  id: string
  name: string
  description: string
}

export type ContributorRoleSort = 'name' | 'createdAt' | 'modifiedAt'
export type ContributorRoleIncludes = 'library'
