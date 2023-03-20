import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { Includes } from '@/types/tankobon-entity'
import { getOneStore } from '@/services/tankobon-stores'
import { StoreEntity } from '@/types/tankobon-store'

interface UseStoreQueryOptions<S = StoreEntity> extends UseQueryOptions<StoreEntity, ErrorResponse, S> {
  storeId: MaybeRef<string>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useStoreQuery<S = StoreEntity>(
  options: UseStoreQueryOptions<S>
) {
  return useQuery<StoreEntity, ErrorResponse, S>({
    queryKey: ['store', { id: options.storeId }],
    queryFn: async () => {
      return await getOneStore({
        storeId: unref(options.storeId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
