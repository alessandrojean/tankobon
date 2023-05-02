import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { SeriesUpdate } from '@/types/tankobon-series'
import { updateOneSeries } from '@/services/tankobon-series'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateSeriesMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, SeriesUpdate>({
    mutationFn: updateOneSeries,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['series'])
      queryClient.invalidateQueries(['series', { id }])
    },
  })
}
