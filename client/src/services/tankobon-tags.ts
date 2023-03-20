import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { 
  type ErrorResponse,
  type EntityResponse,
  TankobonApiError,
PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  TagCreation,
  TagEntity,
  TagIncludes,
  TagSort,
  TagUpdate
} from '@/types/tankobon-tag'
import { Paginated } from '@/types/tankobon-api'

type TagOnly = EntityResponse<TagEntity>
type TagPaginated = PaginatedResponse<TagEntity>

export interface GetAllTagsByLibraryParameters extends Paginated<TagSort> {
  libraryId: string,
  search?: string,
  includes?: TagIncludes[]
}

export async function getAllTagsByLibrary(options: GetAllTagsByLibraryParameters): Promise<TagPaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = search && search.length > 2 ? search : undefined

  try {
    const { data: tags } = await api.get<TagPaginated>(`libraries/${libraryId}/tags`, {
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

    return tags
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneTag(tag: TagCreation): Promise<TagEntity> {
  try {
    const { data } = await api.post<TagOnly>('tags', tag)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneTagParameters {
  tagId?: string,
  includes?: TagIncludes[]
}

export async function getOneTag({ tagId, includes }: GetOneTagParameters): Promise<TagEntity> {
  try {
    const { data: tag } = await api.get<TagOnly>(`tags/${tagId}`, {
      params: {
        includes: includes?.join(','),
      }
    })

    return tag.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneTag(tag: TagUpdate): Promise<void> {
  try {
    await api.put(`tags/${tag.id}`, tag)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneTag(tagId: string): Promise<void> {
  try {
    await api.delete(`tags/${tagId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
