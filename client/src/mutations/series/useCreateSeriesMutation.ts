import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneSeries } from '@/services/tankobon-series'
import type { SeriesCreation, SeriesEntity } from '@/types/tankobon-series'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateSeriesMutation() {
  const queryClient = useQueryClient()

  return useMutation<SeriesEntity, ErrorResponse, SeriesCreation>({
    mutationFn: addOneSeries,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['series', { libraryId: creation.library }])
    }
  })
}
