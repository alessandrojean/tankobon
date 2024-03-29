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

export interface GetAllBooksBySeriesParameters extends Paginated<BookSort> {
  seriesId: string
  includes?: BookIncludes[]
}

export async function getAllBooksBySeries(options: GetAllBooksBySeriesParameters): Promise<BookPaginated> {
  const { seriesId, includes, page, size, sort } = options

  try {
    const { data: books } = await api.get<BookPaginated>(`series/${seriesId}/books`, {
      params: {
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

export interface GetAllBooksByPublisherParameters extends Paginated<BookSort> {
  publisherId: string
  includes?: BookIncludes[]
}

export async function getAllBooksByPublisher(options: GetAllBooksByPublisherParameters): Promise<BookPaginated> {
  const { publisherId, includes, page, size, sort } = options

  try {
    const { data: books } = await api.get<BookPaginated>(`publishers/${publisherId}/books`, {
      params: {
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

export interface GetAllBooksByStoreParameters extends Paginated<BookSort> {
  storeId: string
  includes?: BookIncludes[]
}

export async function getAllBooksByStore(options: GetAllBooksByStoreParameters): Promise<BookPaginated> {
  const { storeId, includes, page, size, sort } = options

  try {
    const { data: books } = await api.get<BookPaginated>(`stores/${storeId}/books`, {
      params: {
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

export interface GetAllBooksByPersonParameters extends Paginated<BookSort> {
  personId: string
  includes?: BookIncludes[]
}

export async function getAllBooksByPerson(options: GetAllBooksByPersonParameters): Promise<BookPaginated> {
  const { personId, includes, page, size, sort } = options

  try {
    const { data: books } = await api.get<BookPaginated>(`people/${personId}/books`, {
      params: {
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

export interface UploadBookCoverOptions {
  bookId: string
  cover: File
}

export async function uploadBookCover(options: UploadBookCoverOptions): Promise<void> {
  try {
    await api.postForm(`books/${options.bookId}/cover`, {
      cover: options.cover,
    })
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

export async function deleteOneBook(bookId: string): Promise<void> {
  try {
    await api.delete(`books/${bookId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteBookCover(bookId: string): Promise<void> {
  try {
    await api.delete(`books/${bookId}/cover`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
