import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { BookEntity } from '@/types/tankobon-book'
import type { GetAllBooksByStoreParameters } from '@/services/tankobon-books'
import { getAllBooksByStore } from '@/services/tankobon-books'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseStoreBooksQueryOptions<S = PaginatedResponse<BookEntity>> =
  UseQueryOptions<PaginatedResponse<BookEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllBooksByStoreParameters>

type ErrorResponse = TankobonApiError | Error

export default function useSeriesBooksQuery<S = PaginatedResponse<BookEntity>>(
  options: UseStoreBooksQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<BookEntity>, ErrorResponse, S>({
    queryKey: [
      'books',
      {
        storeId: options.storeId,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      },
    ],
    queryFn: async () => {
      return await getAllBooksByStore({
        storeId: unref(options.storeId),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
