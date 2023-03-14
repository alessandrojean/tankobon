import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import type { Includes } from '@/types/tankobon-entity'
import type { AddOneLibrary, TankobonLibraryEntity } from '@/types/tankobon-library'
import { 
  type TankobonSuccessCollectionResponse,
  type TankobonErrorResponse,
  type TankobonSuccessEntityResponse,
  TankobonApiError,
} from '@/types/tankobon-response'

type LibraryOnly = TankobonSuccessEntityResponse<TankobonLibraryEntity>
type LibraryCollection = TankobonSuccessCollectionResponse<TankobonLibraryEntity>

export interface GetAllLibrariesParameters {
  ownerId?: string,
  includes?: Includes
}

export async function getAllLibraries(options: GetAllLibrariesParameters): Promise<TankobonLibraryEntity[]> {
  try {
    const { data: libraries } = await api.get<LibraryCollection>('libraries', {
      params: {
        ownerId: options.ownerId,
        includes: options.includes?.join(','),
      },
    })

    return libraries.data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneLibrary(library: AddOneLibrary): Promise<TankobonLibraryEntity> {
  try {
    const { data } = await api.post<LibraryOnly>('libraries', library)

    return data.data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
