import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { PaginatedResponse, TankobonApiError, type ErrorResponse } from '@/types/tankobon-response'
import type { UserEntity, UserIncludes, UserSort, UserSuccessResponse } from '@/types/tankobon-user'
import type { Paginated } from '@/types/tankobon-api'

export async function getMeWithAuth(email: string, password: string): Promise<UserEntity> {
  try {
    const { data: me } = await api.get<UserSuccessResponse>('users/me', {
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

export async function getMe(): Promise<UserEntity> {
  try {
    const { data: me } = await api.get<UserSuccessResponse>('users/me', {
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

export interface GetAllUsersOptions extends Paginated<UserSort> {
  includes?: UserIncludes[],
}

export async function getAllUsers(options?: GetAllUsersOptions): Promise<PaginatedResponse<UserEntity>> {
  try {
    const { data } = await api.get<PaginatedResponse<UserEntity>>('users', {
      params: {
        ...options,
        sort: options?.sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        })
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
