import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { CollectionEntity } from '@/types/tankobon-collection'
import type { GetAllCollectionsByLibraryParameters } from '@/services/tankobon-collections'
import { getAllCollectionsByLibrary } from '@/services/tankobon-collections'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryCollectionsQueryOptions<S = CollectionEntity[]> =
  UseQueryOptions<CollectionEntity[], ErrorResponse, S> &
  MaybeRefDeep<Omit<GetAllCollectionsByLibraryParameters, 'unpaged'>>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryCollectionsUnpagedQuery<S = CollectionEntity[]>(
  options: UseLibraryCollectionsQueryOptions<S>,
) {
  return useQuery<CollectionEntity[], ErrorResponse, S>({
    queryKey: [
      'collections',
      {
        libraryId: options.libraryId,
        search: options.search,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
        unpaged: true,
      },
    ],
    queryFn: async () => {
      const collections = await getAllCollectionsByLibrary({
        libraryId: unref(options.libraryId),
        search: unref(options.search),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
        unpaged: true,
      })

      return collections.data
    },
    ...options,
  })
}
