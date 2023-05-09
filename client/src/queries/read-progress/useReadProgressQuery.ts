import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ReadProgressEntity, ReadProgressIncludes } from '@/types/tankobon-read-progress'
import { getOneReadProgress } from '@/services/tankobon-read-progresses'

interface UseReadProgressQueryOptions<S = ReadProgressEntity> extends UseQueryOptions<ReadProgressEntity, ErrorResponse, S> {
  readProgressId: MaybeRef<string>
  includes?: MaybeRef<ReadProgressIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function useReadProgressQuery<S = ReadProgressEntity>(
  options: UseReadProgressQueryOptions<S>,
) {
  return useQuery<ReadProgressEntity, ErrorResponse, S>({
    queryKey: ['read-progress', { id: options.readProgressId }],
    queryFn: async () => {
      return await getOneReadProgress({
        readProgressId: unref(options.readProgressId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
