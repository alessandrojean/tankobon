import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { getOneCollection } from '@/services/tankobon-collections'
import { CollectionEntity, CollectionIncludes } from '@/types/tankobon-collection'

interface UseCollectionQueryOptions<S = CollectionEntity> extends UseQueryOptions<CollectionEntity, ErrorResponse, S> {
  collectionId: MaybeRef<string>,
  includes?: MaybeRef<CollectionIncludes[]>,
}

type ErrorResponse = TankobonApiError | Error

export default function useCollectionQuery<S = CollectionEntity>(
  options: UseCollectionQueryOptions<S>
) {
  return useQuery<CollectionEntity, ErrorResponse, S>({
    queryKey: ['collection', { id: options.collectionId }],
    queryFn: async () => {
      return await getOneCollection({
        collectionId: unref(options.collectionId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
