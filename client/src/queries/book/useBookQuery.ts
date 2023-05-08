import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import { getOneBook } from '@/services/tankobon-books'
import type { BookEntity, BookIncludes } from '@/types/tankobon-book'

interface UseBookQueryOptions<S = BookEntity> extends UseQueryOptions<BookEntity, ErrorResponse, S> {
  bookId: MaybeRef<string>
  includes?: MaybeRef<BookIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function useBookQuery<S = BookEntity>(
  options: UseBookQueryOptions<S>,
) {
  return useQuery<BookEntity, ErrorResponse, S>({
    queryKey: ['book', { id: options.bookId, includes: options.includes }],
    queryFn: async () => {
      return await getOneBook({
        bookId: unref(options.bookId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
