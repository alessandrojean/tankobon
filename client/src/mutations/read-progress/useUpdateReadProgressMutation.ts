import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ReadProgressUpdate } from '@/types/tankobon-read-progress'
import { updateOneReadProgress } from '@/services/tankobon-read-progresses'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateReadProgressMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, ReadProgressUpdate>({
    mutationFn: updateOneReadProgress,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['read-progresses'])
      queryClient.invalidateQueries(['read-progress', { id }])
    },
  })
}
