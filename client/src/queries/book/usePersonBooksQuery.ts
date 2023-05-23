import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { BookEntity } from '@/types/tankobon-book'
import type { GetAllBooksByPersonParameters } from '@/services/tankobon-books'
import { getAllBooksByPerson } from '@/services/tankobon-books'
import type { MaybeRefDeep } from '@/types/reactivity'

type UsePersonBooksQueryOptions<S = PaginatedResponse<BookEntity>> =
  UseQueryOptions<PaginatedResponse<BookEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllBooksByPersonParameters>

type ErrorResponse = TankobonApiError | Error

export default function usePersonBooksQuery<S = PaginatedResponse<BookEntity>>(
  options: UsePersonBooksQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<BookEntity>, ErrorResponse, S>({
    queryKey: [
      'books',
      {
        personId: options.personId,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      },
    ],
    queryFn: async () => {
      return await getAllBooksByPerson({
        personId: unref(options.personId),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
