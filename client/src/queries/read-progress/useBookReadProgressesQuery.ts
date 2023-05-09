import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { CollectionResponse, TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRefDeep } from '@/types/reactivity'
import type { ReadProgressEntity } from '@/types/tankobon-read-progress'
import type { GetReadProgressesByBookParameters } from '@/services/tankobon-read-progresses'
import { getReadProgressesByBook } from '@/services/tankobon-read-progresses'

type UseBookReadProgressesQueryOptions<S = CollectionResponse<ReadProgressEntity>> =
  UseQueryOptions<CollectionResponse<ReadProgressEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetReadProgressesByBookParameters>

type ErrorResponse = TankobonApiError | Error

export default function useBookReadProgressesQuery<S = CollectionResponse<ReadProgressEntity>>(
  options: UseBookReadProgressesQueryOptions<S>,
) {
  return useQuery<CollectionResponse<ReadProgressEntity>, ErrorResponse, S>({
    queryKey: [
      'read-progresses',
      {
        bookId: options.bookId,
        includes: options.includes,
        sort: options.sort,
      },
    ],
    queryFn: async () => {
      return await getReadProgressesByBook({
        bookId: unref(options.bookId),
        includes: unref(options.includes),
        sort: unref(options.sort),
      })
    },
    ...options,
  })
}
