import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneReadProgress } from '@/services/tankobon-read-progresses'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteReadProgressMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneReadProgress,
    onSuccess(_, readProgressId) {
      queryClient.invalidateQueries(['read-progresses'])
      queryClient.invalidateQueries(['read-progress', { id: readProgressId }])
    },
  })
}
