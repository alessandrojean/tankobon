import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { BookEntity } from '@/types/tankobon-book'
import type { GetAllBooksByLibraryParameters } from '@/services/tankobon-books'
import { getAllBooksByLibrary } from '@/services/tankobon-books'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryBooksQueryOptions<S = PaginatedResponse<BookEntity>> =
  UseQueryOptions<PaginatedResponse<BookEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllBooksByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryBooksQuery<S = PaginatedResponse<BookEntity>>(
  options: UseLibraryBooksQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<BookEntity>, ErrorResponse, S>({
    queryKey: [
      'books',
      {
        libraryId: options.libraryId,
        search: options.search,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      },
    ],
    queryFn: async () => {
      return await getAllBooksByLibrary({
        libraryId: unref(options.libraryId),
        search: unref(options.search),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
