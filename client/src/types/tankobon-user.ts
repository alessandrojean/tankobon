import { Entity } from './tankobon-entity'
import { EntityResponse } from './tankobon-response'

export type TankobonUserEntity = Entity<TankobonUserAttributes> & {
  type: 'USER'
}

export interface TankobonUserAttributes {
  name: string,
  email: string,
  biography: string,
  roles: TankobonUserRole[]
}

export type TankobonUserRole = 'ROLE_ADMIN' | 'ROLE_USER'

export type TankobonUserSuccessResponse = EntityResponse<TankobonUserEntity>