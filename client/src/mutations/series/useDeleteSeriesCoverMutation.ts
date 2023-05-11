import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteSeriesCover } from '@/services/tankobon-series'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteSeriesCoverMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteSeriesCover,
    async onSuccess(_, seriesId) {
      queryClient.invalidateQueries(['series'])
      queryClient.invalidateQueries(['series', { id: seriesId }])
    },
  })
}
