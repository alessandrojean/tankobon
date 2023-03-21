import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { ExternalBookEntity } from '@/types/tankobon-external-book'
import { getAllSources } from '@/services/tankobon-importer'
import { ImporterSourceEntity } from '@/types/tankobon-importer-source'

type UseImporterSearchQueryOptions<S = ImporterSourceEntity[]> =
  UseQueryOptions<ImporterSourceEntity[], ErrorResponse, S>

type ErrorResponse = TankobonApiError | Error

export default function useExternalSourcesQuery<S = ExternalBookEntity[]>(
  options?: UseImporterSearchQueryOptions<S>
) {
  return useQuery<ImporterSourceEntity[], ErrorResponse, S>({
    queryKey: ['external-sources'],
    queryFn: getAllSources,
    staleTime: Infinity,
    ...options,
  })
}
