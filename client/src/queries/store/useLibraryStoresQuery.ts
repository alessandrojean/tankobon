import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { StoreEntity } from '@/types/tankobon-store'
import type { GetAllStoresByLibraryParameters } from '@/services/tankobon-stores'
import { getAllStoresByLibrary } from '@/services/tankobon-stores'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryStoresQueryOptions<S = PaginatedResponse<StoreEntity>> =
  UseQueryOptions<PaginatedResponse<StoreEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllStoresByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryStoresQuery<S = PaginatedResponse<StoreEntity>>(
  options: UseLibraryStoresQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<StoreEntity>, ErrorResponse, S>({
    queryKey: [
      'stores',
      {
        libraryId: options.libraryId,
        search: options.search,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
        unpaged: options.unpaged,
      },
    ],
    queryFn: async () => {
      return await getAllStoresByLibrary({
        libraryId: unref(options.libraryId),
        search: unref(options.search),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
        unpaged: unref(options.unpaged),
      })
    },
    ...options,
  })
}
