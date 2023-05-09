import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { TankobonApiError } from '@/types/tankobon-response'
import type {
  CollectionResponse,
  EntityResponse,
  ErrorResponse,
} from '@/types/tankobon-response'
import type {
  ReadProgressCreation,
  ReadProgressEntity,
  ReadProgressIncludes,
  ReadProgressSort,
  ReadProgressUpdate,
} from '@/types/tankobon-read-progress'
import type { Sort } from '@/types/tankobon-api'

type ReadProgressOnly = EntityResponse<ReadProgressEntity>
type ReadProgressCollection = CollectionResponse<ReadProgressEntity>

export interface GetReadProgressesByBookParameters {
  bookId: string
  includes?: ReadProgressIncludes[]
  sort?: Sort<ReadProgressSort>[]
}

export async function getReadProgressesByBook(options: GetReadProgressesByBookParameters): Promise<ReadProgressCollection> {
  const { bookId, includes, sort } = options

  try {
    const { data: readProgresses } = await api.get<ReadProgressCollection>(`books/${bookId}/read-progresses`, {
      params: {
        includes: includes?.join(','),
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        }),
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return readProgresses
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetReadProgressesByUserParameters {
  userId: string
  includes?: ReadProgressIncludes[]
}

export async function getReadProgressesByUser(options: GetReadProgressesByUserParameters): Promise<ReadProgressCollection> {
  const { userId, includes } = options

  try {
    const { data: readProgresses } = await api.get<ReadProgressCollection>(`users/${userId}/read-progresses`, {
      params: { includes: includes?.join(',') },
    })

    return readProgresses
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneReadProgress(readProgress: ReadProgressCreation): Promise<ReadProgressEntity> {
  try {
    const { data } = await api.post<ReadProgressOnly>('read-progresses', readProgress)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneReadProgressParameters {
  readProgressId?: string
  includes?: ReadProgressIncludes[]
}

export async function getOneReadProgress({ readProgressId, includes }: GetOneReadProgressParameters): Promise<ReadProgressEntity> {
  try {
    const { data: readProgress } = await api.get<ReadProgressOnly>(`read-progresses/${readProgressId}`, {
      params: { includes: includes?.join(',') },
    })

    return readProgress.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneReadProgress(readProgress: ReadProgressUpdate): Promise<void> {
  try {
    await api.put(`read-progress/${readProgress.id}`, readProgress)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneReadProgress(readProgressId: string): Promise<void> {
  try {
    await api.delete(`read-progresses/${readProgressId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
