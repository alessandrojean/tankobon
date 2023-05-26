import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import type { LibraryCreation, LibraryEntity, LibraryIncludes, LibrarySort, LibraryUpdate } from '@/types/tankobon-library'
import type {
  CollectionResponse,
  EntityResponse,
  ErrorResponse,
  PaginatedResponse,
} from '@/types/tankobon-response'
import {
  TankobonApiError,
} from '@/types/tankobon-response'
import type { Paginated } from '@/types/tankobon-api'

type LibraryOnly = EntityResponse<LibraryEntity>
type LibraryCollection = CollectionResponse<LibraryEntity>
type LibraryPaginated = PaginatedResponse<LibraryEntity>

export interface GetAllLibrariesParameters {
  ownerId?: string
  includes?: LibraryIncludes[]
}

export async function getAllLibraries(options: GetAllLibrariesParameters): Promise<LibraryEntity[]> {
  try {
    const { data: libraries } = await api.get<LibraryCollection>('libraries', {
      params: {
        ownerId: options.ownerId,
        includes: options.includes?.join(','),
      },
    })

    return libraries.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetAllLibrariesByUserParameters extends Paginated<LibrarySort> {
  userId?: string
  includeShared?: boolean
  includes?: LibraryIncludes[]
}

export async function getAllLibrariesByUser(options: GetAllLibrariesByUserParameters): Promise<LibraryPaginated> {
  const { userId, includes, includeShared, page, size, sort, unpaged } = options

  try {
    const { data: libraries } = await api.get<LibraryPaginated>(`users/${userId}/libraries`, {
      params: {
        includes: includes?.join(','),
        includeShared,
        page,
        size,
        unpaged: unpaged ?? false,
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        }),
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return libraries
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneLibrary(library: LibraryCreation): Promise<LibraryEntity> {
  try {
    const { data } = await api.post<LibraryOnly>('libraries', library)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneLibraryParameters {
  libraryId?: string
  includes?: LibraryIncludes[]
}

export async function getOneLibrary({ libraryId, includes }: GetOneLibraryParameters): Promise<LibraryEntity> {
  try {
    const { data: library } = await api.get<EntityResponse<LibraryEntity>>(`libraries/${libraryId}`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return library.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneLibrary(library: LibraryUpdate): Promise<void> {
  try {
    await api.put(`libraries/${library.id}`, library)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneLibrary(libraryId: string): Promise<void> {
  try {
    await api.delete(`libraries/${libraryId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
