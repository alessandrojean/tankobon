import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOnePerson } from '@/services/tankobon-people'

type ErrorResponse = TankobonApiError | Error

export default function useDeletePersonMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOnePerson,
    onSuccess(_, personId) {
      queryClient.invalidateQueries(['people'])
      queryClient.invalidateQueries(['person', { id: personId }])
    }
  })
}
