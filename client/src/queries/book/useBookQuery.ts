import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { getOneBook } from '@/services/tankobon-books'
import { BookEntity, BookIncludes } from '@/types/tankobon-book'

interface UseBookQueryOptions<S = BookEntity> extends UseQueryOptions<BookEntity, ErrorResponse, S> {
  bookId: MaybeRef<string>,
  includes?: MaybeRef<BookIncludes[]>,
}

type ErrorResponse = TankobonApiError | Error

export default function useBookQuery<S = BookEntity>(
  options: UseBookQueryOptions<S>
) {
  return useQuery<BookEntity, ErrorResponse, S>({
    queryKey: ['book', { id: options.bookId }],
    queryFn: async () => {
      return await getOneBook({
        bookId: unref(options.bookId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
