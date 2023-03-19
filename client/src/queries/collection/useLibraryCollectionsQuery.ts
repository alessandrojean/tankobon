import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { Includes } from '@/types/tankobon-entity'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRef } from '@vueuse/core'
import { CollectionEntity } from '@/types/tankobon-collection'
import { getAllCollectionsByLibrary, GetAllCollectionsByLibraryParameters } from '@/services/tankobon-collections'
import { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryCollectionsQueryOptions<S = PaginatedResponse<CollectionEntity>> =
  UseQueryOptions<PaginatedResponse<CollectionEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllCollectionsByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryCollectionsQuery<S = PaginatedResponse<CollectionEntity>>(
  options: UseLibraryCollectionsQueryOptions<S>
) {
  return useQuery<PaginatedResponse<CollectionEntity>, ErrorResponse, S>({
    queryKey: [
      'collections',
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
      return await getAllCollectionsByLibrary({ 
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
