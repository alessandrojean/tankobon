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
  BookEntity,
  BookIncludes,
  BookSort,
  BookUpdate,
} from '@/types/tankobon-book'
import type { Paginated } from '@/types/tankobon-api'

type BookOnly = EntityResponse<BookEntity>
type BookPaginated = PaginatedResponse<BookEntity>

export interface GetAllBooksByLibraryParameters extends Paginated<BookSort> {
  libraryId: string
  search?: string
  includes?: BookIncludes[]
}

export async function getAllBooksByLibrary(options: GetAllBooksByLibraryParameters): Promise<BookPaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = (search && search.length > 2) ? search : undefined

  try {
    const { data: books } = await api.get<BookPaginated>(`libraries/${libraryId}/books`, {
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

    return books
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneBookParameters {
  bookId?: string
  includes?: BookIncludes[]
}

export async function getOneBook({ bookId, includes }: GetOneBookParameters): Promise<BookEntity> {
  try {
    const { data: book } = await api.get<BookOnly>(`books/${bookId}`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return book.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneBook(book: BookUpdate): Promise<BookEntity> {
  try {
    const { data } = await api.post<BookOnly>('books', book)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneBook(book: BookUpdate): Promise<void> {
  try {
    await api.put(`books/${book.id}`, book)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
