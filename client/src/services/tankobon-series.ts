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
  SeriesCreation,
  SeriesEntity,
  SeriesIncludes,
  SeriesSort,
  SeriesUpdate,
} from '@/types/tankobon-series'
import type { Paginated } from '@/types/tankobon-api'

type SeriesOnly = EntityResponse<SeriesEntity>
type SeriesPaginated = PaginatedResponse<SeriesEntity>

export interface GetAllSeriesByLibraryParameters extends Paginated<SeriesSort> {
  libraryId: string
  search?: string
  includes?: SeriesIncludes[]
}

export async function getAllSeriesByLibrary(options: GetAllSeriesByLibraryParameters): Promise<SeriesPaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = (search && search.length > 2) ? search : undefined

  try {
    const { data: seriess } = await api.get<SeriesPaginated>(`libraries/${libraryId}/series`, {
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

    return seriess
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneSeries(series: SeriesCreation): Promise<SeriesEntity> {
  try {
    const { data } = await api.post<SeriesOnly>('series', series)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneSeriesParameters {
  seriesId?: string
  includes?: SeriesIncludes[]
}

export async function getOneSeries({ seriesId, includes }: GetOneSeriesParameters): Promise<SeriesEntity> {
  try {
    const { data: series } = await api.get<SeriesOnly>(`series/${seriesId}`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return series.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneSeries(series: SeriesUpdate): Promise<void> {
  try {
    await api.put(`series/${series.id}`, series)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneSeries(seriesId: string): Promise<void> {
  try {
    await api.delete(`series/${seriesId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
