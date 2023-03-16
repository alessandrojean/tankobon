import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import type { Includes } from '@/types/tankobon-entity'
import type { AddOneLibrary, LibraryEntity } from '@/types/tankobon-library'
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
  includes?: Includes
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

export async function addOneLibrary(library: AddOneLibrary): Promise<LibraryEntity> {
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
