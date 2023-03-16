import { Entity } from './tankobon-entity'
import { EntityResponse } from './tankobon-response'

export type UserEntity = Entity<UserAttributes> & {
  type: 'USER'
}

export interface UserAttributes {
  name: string,
  email: string,
  biography: string,
  roles: UserRole[],
  createdAt: string,
}

export type UserRole = 'ROLE_ADMIN' | 'ROLE_USER'
export type UserIncludes = 'avatar'
export type UserSort = 'name' | 'createdAt' | 'modifiedAt'

export type UserSuccessResponse = EntityResponse<UserEntity>