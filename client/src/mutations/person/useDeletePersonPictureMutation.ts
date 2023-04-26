import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deletePersonPicture } from '@/services/tankobon-people'

type ErrorResponse = TankobonApiError | Error

export default function useDeletePersonPictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deletePersonPicture,
    async onSuccess(_, personId) {
      queryClient.invalidateQueries(['people'])
      queryClient.invalidateQueries(['person', { id: personId }])
    }
  })
}
