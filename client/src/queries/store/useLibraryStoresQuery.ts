import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import { StoreEntity } from '@/types/tankobon-store'
import { getAllStoresByLibrary, GetAllStoresByLibraryParameters } from '@/services/tankobon-stores'
import { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryStoresQueryOptions<S = PaginatedResponse<StoreEntity>> =
  UseQueryOptions<PaginatedResponse<StoreEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllStoresByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryStoresQuery<S = PaginatedResponse<StoreEntity>>(
  options: UseLibraryStoresQueryOptions<S>
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
      }
    ],
    queryFn: async () => {
      return await getAllStoresByLibrary({ 
        libraryId: unref(options.libraryId),
        search: unref(options.search),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
