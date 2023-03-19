import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import { SeriesEntity } from '@/types/tankobon-series'
import { getAllSeriesByLibrary, GetAllSeriesByLibraryParameters } from '@/services/tankobon-series'
import { MaybeRefDeep } from '@/types/reactivity'

type UseLibrarySeriesQueryOptions<S = PaginatedResponse<SeriesEntity>> =
  UseQueryOptions<PaginatedResponse<SeriesEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllSeriesByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibrarySeriesQuery<S = PaginatedResponse<SeriesEntity>>(
  options: UseLibrarySeriesQueryOptions<S>
) {
  return useQuery<PaginatedResponse<SeriesEntity>, ErrorResponse, S>({
    queryKey: [
      'series',
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
      return await getAllSeriesByLibrary({ 
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
