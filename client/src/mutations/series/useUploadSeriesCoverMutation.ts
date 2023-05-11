import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { UploadSeriesCoverOptions } from '@/services/tankobon-series'
import { uploadSeriesCover } from '@/services/tankobon-series'

type ErrorResponse = TankobonApiError | Error

export default function useUploadSeriesCoverMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, UploadSeriesCoverOptions>({
    mutationFn: uploadSeriesCover,
    async onSuccess(_, { seriesId }) {
      queryClient.invalidateQueries(['series'])
      queryClient.invalidateQueries(['series', { id: seriesId }])
    },
  })
}
