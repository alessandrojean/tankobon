import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import type { LibraryCreation, LibraryEntity, LibraryIncludes, LibraryUpdate } from '@/types/tankobon-library'
import { 
  type CollectionResponse,
  type ErrorResponse,
  type EntityResponse,
  TankobonApiError,
} from '@/types/tankobon-response'

type LibraryOnly = EntityResponse<LibraryEntity>
type LibraryCollection = CollectionResponse<LibraryEntity>

export interface GetAllLibrariesParameters {
  ownerId?: string,
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

export interface GetAllLibrariesByUserParameters {
  userId?: string,
  includeShared?: boolean,
  includes?: LibraryIncludes[]
}

export async function getAllLibrariesByUser(options: GetAllLibrariesByUserParameters): Promise<LibraryEntity[]> {
  const { userId, includes, includeShared } = options

  try {
    const { data: libraries } = await api.get<LibraryCollection>(`users/${userId}/libraries`, {
      params: {
        includes: includes?.join(','),
        includeShared: includeShared,
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
  libraryId?: string,
  includes?: LibraryIncludes[]
}

export async function getOneLibrary({ libraryId, includes }: GetOneLibraryParameters): Promise<LibraryEntity> {
  try {
    const { data: library } = await api.get<EntityResponse<LibraryEntity>>(`libraries/${libraryId}`, {
      params: {
        includes: includes?.join(','),
      }
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
