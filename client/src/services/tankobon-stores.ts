import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { 
  type ErrorResponse,
  type EntityResponse,
  TankobonApiError,
PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  StoreCreation,
  StoreEntity,
  StoreIncludes,
  StoreSort,
  StoreUpdate
} from '@/types/tankobon-store'
import { Paginated } from '@/types/tankobon-api'

type StoreOnly = EntityResponse<StoreEntity>
type StorePaginated = PaginatedResponse<StoreEntity>

export interface GetAllStoresByLibraryParameters extends Paginated<StoreSort> {
  libraryId: string,
  search?: string,
  includes?: StoreIncludes[]
}

export async function getAllStoresByLibrary(options: GetAllStoresByLibraryParameters): Promise<StorePaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = search && search.length > 2 ? search : undefined

  try {
    const { data: stores } = await api.get<StorePaginated>(`libraries/${libraryId}/stores`, {
      params: {
        search: searchOrUndefined,
        includes: includes?.join(','),
        page,
        size,
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        })
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return stores
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneStore(store: StoreCreation): Promise<StoreEntity> {
  try {
    const { data } = await api.post<StoreOnly>('stores', store)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneStoreParameters {
  storeId?: string,
  includes?: StoreIncludes[]
}

export async function getOneStore({ storeId, includes }: GetOneStoreParameters): Promise<StoreEntity> {
  try {
    const { data: store } = await api.get<StoreOnly>(`stores/${storeId}`, {
      params: {
        includes: includes?.join(','),
      }
    })

    return store.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneStore(store: StoreUpdate): Promise<void> {
  try {
    await api.put(`stores/${store.id}`, store)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneStore(storeId: string): Promise<void> {
  try {
    await api.delete(`stores/${storeId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
