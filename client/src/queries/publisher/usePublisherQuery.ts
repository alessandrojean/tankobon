import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import { getOnePublisher } from '@/services/tankobon-publishers'
import type { PublisherEntity, PublisherIncludes } from '@/types/tankobon-publisher'

interface UsePublisherQueryOptions<S = PublisherEntity> extends UseQueryOptions<PublisherEntity, ErrorResponse, S> {
  publisherId: MaybeRef<string>
  includes?: MaybeRef<PublisherIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function usePublisherQuery<S = PublisherEntity>(
  options: UsePublisherQueryOptions<S>,
) {
  return useQuery<PublisherEntity, ErrorResponse, S>({
    queryKey: ['publisher', { id: options.publisherId }],
    queryFn: async () => {
      return await getOnePublisher({
        publisherId: unref(options.publisherId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
