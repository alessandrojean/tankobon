import { TankobonEntity } from './tankobon-entity'
import { TankobonSuccessEntityResponse } from './tankobon-response'

export type TankobonUserEntity = TankobonEntity<TankobonUserAttributes> & {
  type: 'USER'
}

export interface TankobonUserAttributes {
  name: string,
  email: string,
  biography: string,
  roles: TankobonUserRole[]
}

export type TankobonUserRole = 'ROLE_ADMIN' | 'ROLE_USER'

export type TankobonUserSuccessResponse = TankobonSuccessEntityResponse<TankobonUserEntity>