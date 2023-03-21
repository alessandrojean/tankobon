import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRefDeep } from '@/types/reactivity'
import { ExternalBookEntity } from '@/types/tankobon-external-book'
import { searchByIsbn, SearchByIsbnOptions } from '@/services/tankobon-importer'

type UseImporterSearchQueryOptions<S = ExternalBookEntity[]> =
  UseQueryOptions<ExternalBookEntity[], ErrorResponse, S> &
  MaybeRefDeep<SearchByIsbnOptions>

type ErrorResponse = TankobonApiError | Error

export default function useImporterSearchQuery<S = ExternalBookEntity[]>(
  options: UseImporterSearchQueryOptions<S>
) {
  return useQuery<ExternalBookEntity[], ErrorResponse, S>({
    queryKey: [
      'importer-search',
      {
        isbn: options.isbn,
        sources: options.sources,
        includes: options.includes,
      }
    ],
    queryFn: async () => {
      return await searchByIsbn({ 
        isbn: unref(options.isbn),
        sources: unref(options.sources),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
