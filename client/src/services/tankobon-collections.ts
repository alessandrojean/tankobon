import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { 
  type ErrorResponse,
  type EntityResponse,
  TankobonApiError,
PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  CollectionCreation,
  CollectionEntity,
  CollectionIncludes,
  CollectionSort,
  CollectionUpdate
} from '@/types/tankobon-collection'
import { Paginated } from '@/types/tankobon-api'

type CollectionOnly = EntityResponse<CollectionEntity>
type CollectionPaginated = PaginatedResponse<CollectionEntity>

export interface GetAllCollectionsByLibraryParameters extends Paginated<CollectionSort> {
  libraryId: string,
  search?: string,
  includes?: CollectionIncludes[]
}

export async function getAllCollectionsByLibrary(options: GetAllCollectionsByLibraryParameters): Promise<CollectionPaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = search && search.length > 2 ? search : undefined

  try {
    const { data: collections } = await api.get<CollectionPaginated>(`libraries/${libraryId}/collections`, {
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

    return collections
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneCollection(collection: CollectionCreation): Promise<CollectionEntity> {
  try {
    const { data } = await api.post<CollectionOnly>('collections', collection)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneCollectionParameters {
  collectionId?: string,
  includes?: CollectionIncludes[]
}

export async function getOneCollection({ collectionId, includes }: GetOneCollectionParameters): Promise<CollectionEntity> {
  try {
    const { data: collection } = await api.get<CollectionOnly>(`collections/${collectionId}`, {
      params: {
        includes: includes?.join(','),
      }
    })

    return collection.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneCollection(collection: CollectionUpdate): Promise<void> {
  try {
    await api.put(`collections/${collection.id}`, collection)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneCollection(collectionId: string): Promise<void> {
  try {
    await api.delete(`collections/${collectionId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
