import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOnePublisher } from '@/services/tankobon-publishers'

type ErrorResponse = TankobonApiError | Error

export default function useDeletePublisherMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOnePublisher,
    onSuccess(_, publisherId) {
      queryClient.invalidateQueries(['publishers'])
      queryClient.invalidateQueries(['publisher', { id: publisherId }])
    },
  })
}
