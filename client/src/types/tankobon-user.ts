import type { Entity } from './tankobon-entity'
import type { EntityResponse } from './tankobon-response'

export type UserEntity = Entity<UserAttributes> & {
  type: 'USER'
}

export interface UserAttributes {
  name: string
  email: string
  biography: string
  roles: UserRole[]
  createdAt: string
}

export type UserRole = 'ROLE_ADMIN' | 'ROLE_USER'
export type UserIncludes = 'avatar'
export type UserSort = 'name' | 'createdAt' | 'modifiedAt'

export type UserSuccessResponse = EntityResponse<UserEntity>

export interface EmailAvailability {
  isAvailable: boolean
}

export interface UserCreation {
  email: string
  name: string
  password: string
  roles: UserRole[]
}

export interface UserUpdate {
  id: string
  email: string
  name: string
  biography: string
  roles: UserRole[]
}
