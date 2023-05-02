import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ExternalBookEntity } from '@/types/tankobon-external-book'
import { getAllSources } from '@/services/tankobon-importer'
import type { ImporterSourceEntity } from '@/types/tankobon-importer-source'

type UseImporterSearchQueryOptions<S = ImporterSourceEntity[]> =
  UseQueryOptions<ImporterSourceEntity[], ErrorResponse, S>

type ErrorResponse = TankobonApiError | Error

export default function useExternalSourcesQuery<S = ExternalBookEntity[]>(
  options?: UseImporterSearchQueryOptions<S>,
) {
  return useQuery<ImporterSourceEntity[], ErrorResponse, S>({
    queryKey: ['external-sources'],
    queryFn: getAllSources,
    staleTime: Infinity,
    ...options,
  })
}
