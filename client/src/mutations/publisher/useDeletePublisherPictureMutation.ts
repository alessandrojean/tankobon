import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deletePublisherPicture } from '@/services/tankobon-publishers'

type ErrorResponse = TankobonApiError | Error

export default function useDeletePublisherPictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deletePublisherPicture,
    async onSuccess(_, publisherId) {
      queryClient.invalidateQueries(['publishers'])
      queryClient.invalidateQueries(['publisher', { id: publisherId }])
    },
  })
}
