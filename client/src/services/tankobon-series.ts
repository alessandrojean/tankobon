import { isAxiosError } from 'axios'
import groupBy from 'lodash.groupby'
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
import type { AlternativeName } from '@/types/tankobon-alternative-name'

type SeriesOnly = EntityResponse<SeriesEntity>
type SeriesPaginated = PaginatedResponse<SeriesEntity>

export interface GetAllSeriesByLibraryParameters extends Paginated<SeriesSort> {
  libraryId: string
  search?: string
  includes?: SeriesIncludes[]
}

export async function getAllSeriesByLibrary(options: GetAllSeriesByLibraryParameters): Promise<SeriesPaginated> {
  const { libraryId, includes, page, size, sort, search, unpaged } = options
  const searchOrUndefined = (search && search.length > 2) ? search : undefined

  try {
    const { data: seriess } = await api.get<SeriesPaginated>(`libraries/${libraryId}/series`, {
      params: {
        search: searchOrUndefined,
        includes: includes?.join(','),
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

export interface UploadSeriesCoverOptions {
  seriesId: string
  cover: File
}

export async function uploadSeriesCover(options: UploadSeriesCoverOptions): Promise<void> {
  try {
    await api.postForm(`series/${options.seriesId}/cover`, {
      cover: options.cover,
    })
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

export async function deleteSeriesCover(seriesId: string): Promise<void> {
  try {
    await api.delete(`series/${seriesId}/cover`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

function getFirstKeyAvailable(alternativeNames: Record<string, AlternativeName[]>, ...keys: string[]) {
  const names = keys.map(key => alternativeNames[key]?.[0])

  return names.find(name => name !== undefined && name.name.length > 0)
}

// https://unicode.org/iso15924/iso15924-codes.html
export function getPossibleOriginalName(series: SeriesEntity | undefined | null, alternativeNames: Record<string, AlternativeName[]>) {
  if (!series) {
    return undefined
  }

  const type = series.attributes.type

  if (type === 'MANGA' || type === 'LIGHT_NOVEL') {
    return getFirstKeyAvailable(alternativeNames, 'ja-JP', 'ja-Latn-JP')
  } else if (type === 'MANHWA') {
    return getFirstKeyAvailable(alternativeNames, 'ko-KR', 'ko-KP', 'ko-Latn-KR', 'ko-Latn-KP')
  } else if (type === 'MANHUA') {
    return getFirstKeyAvailable(alternativeNames, 'zh-CN', 'zh-TW', 'zh-HK', 'zh-Hant-TW', 'zh-Hans-CN', 'zh-Latn-CN', 'zh-Latn-TW')
  } else {
    return series.attributes.alternativeNames[0]
  }
}

export function getOriginalName(series: SeriesEntity | undefined | null) {
  if (!series) {
    return undefined
  }

  const { originalLanguage } = series.attributes
  const alternativeNames = groupBy(series.attributes.alternativeNames ?? [], 'language')

  if (!originalLanguage) {
    return getPossibleOriginalName(series, alternativeNames)
  }

  if (alternativeNames[originalLanguage]) {
    return alternativeNames[originalLanguage][0]
  }

  const [language] = originalLanguage.split('-')
  const firstLanguage = Object
    .keys(alternativeNames)
    .find(key => key.startsWith(language))

  return firstLanguage
    ? alternativeNames[firstLanguage][0]
    : series.attributes.alternativeNames[0]
}
