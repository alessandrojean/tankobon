import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { BookEntity } from '@/types/tankobon-book'
import type { GetAllBooksBySeriesParameters } from '@/services/tankobon-books'
import { getAllBooksBySeries } from '@/services/tankobon-books'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseSeriesBooksQueryOptions<S = PaginatedResponse<BookEntity>> =
  UseQueryOptions<PaginatedResponse<BookEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllBooksBySeriesParameters>

type ErrorResponse = TankobonApiError | Error

export default function useSeriesBooksQuery<S = PaginatedResponse<BookEntity>>(
  options: UseSeriesBooksQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<BookEntity>, ErrorResponse, S>({
    queryKey: [
      'books',
      {
        seriesId: options.seriesId,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      },
    ],
    queryFn: async () => {
      return await getAllBooksBySeries({
        seriesId: unref(options.seriesId),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
