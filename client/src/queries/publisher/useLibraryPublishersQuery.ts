import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import { PublisherEntity } from '@/types/tankobon-publisher'
import { getAllPublishersByLibrary, GetAllPublishersByLibraryParameters } from '@/services/tankobon-publishers'
import { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryPublishersQueryOptions<S = PaginatedResponse<PublisherEntity>> =
  UseQueryOptions<PaginatedResponse<PublisherEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllPublishersByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryPublishersQuery<S = PaginatedResponse<PublisherEntity>>(
  options: UseLibraryPublishersQueryOptions<S>
) {
  return useQuery<PaginatedResponse<PublisherEntity>, ErrorResponse, S>({
    queryKey: [
      'publishers',
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
      return await getAllPublishersByLibrary({ 
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
