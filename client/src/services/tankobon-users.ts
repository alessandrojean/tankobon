import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { TankobonApiError, type ErrorResponse } from '@/types/tankobon-response'
import type {
  TankobonUserEntity,
  TankobonUserSuccessResponse
} from '@/types/tankobon-user'

export async function getMeWithAuth(email: string, password: string): Promise<TankobonUserEntity> {
  try {
    const { data: me } = await api.get<TankobonUserSuccessResponse>('users/me', {
      params: { includes: 'avatar' },
      auth: {
        username: email,
        password: password,
      }
    })

    return me.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function getMe(): Promise<TankobonUserEntity> {
  try {
    const { data: me } = await api.get<TankobonUserSuccessResponse>('users/me', {
      params: { includes: 'avatar' },
    })

    return me.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function signOut() {
  try {
    await api.post('sign-out')
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}