import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import {
  TankobonApiError,
} from '@/types/tankobon-response'
import type {
  EntityResponse,
  ErrorResponse,
  PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  PublisherCreation,
  PublisherEntity,
  PublisherIncludes,
  PublisherSort,
  PublisherUpdate,
} from '@/types/tankobon-publisher'
import type { Paginated } from '@/types/tankobon-api'

type PublisherOnly = EntityResponse<PublisherEntity>
type PublisherPaginated = PaginatedResponse<PublisherEntity>

export interface GetAllPublishersByLibraryParameters extends Paginated<PublisherSort> {
  libraryId: string
  search?: string
  includes?: PublisherIncludes[]
}

export async function getAllPublishersByLibrary(options: GetAllPublishersByLibraryParameters): Promise<PublisherPaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = (search && search.length > 2) ? search : undefined

  try {
    const { data: publishers } = await api.get<PublisherPaginated>(`libraries/${libraryId}/publishers`, {
      params: {
        search: searchOrUndefined,
        includes: includes?.join(','),
        page,
        size,
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        }),
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return publishers
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOnePublisher(publisher: PublisherCreation): Promise<PublisherEntity> {
  try {
    const { data } = await api.post<PublisherOnly>('publishers', publisher)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOnePublisherParameters {
  publisherId?: string
  includes?: PublisherIncludes[]
}

export async function getOnePublisher({ publisherId, includes }: GetOnePublisherParameters): Promise<PublisherEntity> {
  try {
    const { data: publisher } = await api.get<PublisherOnly>(`publishers/${publisherId}`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return publisher.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface UploadPublisherPictureOptions {
  publisherId: string
  picture: File
}

export async function uploadPublisherPicture(options: UploadPublisherPictureOptions): Promise<void> {
  try {
    await api.postForm(`publishers/${options.publisherId}/picture`, {
      picture: options.picture,
    })
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOnePublisher(publisher: PublisherUpdate): Promise<void> {
  try {
    await api.put(`publishers/${publisher.id}`, publisher)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOnePublisher(publisherId: string): Promise<void> {
  try {
    await api.delete(`publishers/${publisherId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deletePublisherPicture(publisherId: string): Promise<void> {
  try {
    await api.delete(`publishers/${publisherId}/picture`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
