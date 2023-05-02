import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { TankobonApiError } from '@/types/tankobon-response'
import type { EntityResponse, type ErrorResponse, PaginatedResponse } from '@/types/tankobon-response'
import type { EmailAvailability, UserCreation, UserEntity, UserIncludes, UserSort, UserSuccessResponse, UserUpdate } from '@/types/tankobon-user'
import type { Paginated } from '@/types/tankobon-api'
import type { AuthenticationActivityEntity, AuthenticationActivitySort } from '@/types/tankobon-authentication-activity'

export async function getMeWithAuth(email: string, password: string): Promise<UserEntity> {
  try {
    const { data: me } = await api.get<UserSuccessResponse>('users/me', {
      params: { includes: 'avatar' },
      auth: {
        username: email,
        password,
      },
    })

    return me.data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function getMe(): Promise<UserEntity> {
  try {
    const { data: me } = await api.get<UserSuccessResponse>('users/me', {
      params: { includes: 'avatar' },
    })

    return me.data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function signOut() {
  try {
    await api.post('sign-out')
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export interface GetAllUsersOptions extends Paginated<UserSort> {
  includes?: UserIncludes[]
}

export async function getAllUsers(options?: GetAllUsersOptions): Promise<PaginatedResponse<UserEntity>> {
  try {
    const { data } = await api.get<PaginatedResponse<UserEntity>>('users', {
      params: {
        ...options,
        sort: options?.sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        }),
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function getOneUser(userId: string): Promise<UserEntity> {
  try {
    const { data: user } = await api.get<EntityResponse<UserEntity>>(`users/${userId}`, {
      params: { includes: 'avatar' },
    })

    return user.data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export interface GetAuthenticationActivityFromUserOptions extends Paginated<AuthenticationActivitySort> {
  userId: string
  includes?: UserIncludes[]
}

export async function getAuthenticationActivityFromUser(options: GetAuthenticationActivityFromUserOptions): Promise<PaginatedResponse<AuthenticationActivityEntity>> {
  try {
    const { data } = await api.get<PaginatedResponse<AuthenticationActivityEntity>>(
      `users/${options.userId}/authentication-activity`,
      {
        params: {
          ...options,
          userId: undefined,
          sort: options?.sort?.map(({ property, direction }) => {
            return `${property},${direction}`
          }),
        },
        paramsSerializer: {
          indexes: null,
        },
      },
    )

    return data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function checkEmailAvailability(email: string): Promise<boolean> {
  try {
    const { data } = await api.get<EmailAvailability>(`users/availability/${email}`)

    return data.isAvailable
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function addOneUser(user: UserCreation): Promise<UserEntity> {
  try {
    const { data: createdUser } = await api.post<EntityResponse<UserEntity>>('users', user)

    return createdUser.data
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function deleteUserAvatar(userId: string): Promise<void> {
  try {
    await api.delete(`users/${userId}/avatar`)
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export interface UploadUserAvatarOptions {
  userId: string
  avatar: File
}

export async function uploadUserAvatar(options: UploadUserAvatarOptions): Promise<void> {
  try {
    await api.postForm(`users/${options.userId}/avatar`, {
      avatar: options.avatar,
    })
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function updateUser(user: UserUpdate): Promise<void> {
  try {
    await api.put(`users/${user.id}`, user)
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}

export async function deleteOneUser(userId: string): Promise<void> {
  try {
    await api.delete(`users/${userId}`)
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}
