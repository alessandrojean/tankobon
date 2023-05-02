import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { PersonUpdate } from '@/types/tankobon-person'
import { updateOnePerson } from '@/services/tankobon-people'

type ErrorResponse = TankobonApiError | Error

export default function useUpdatePersonMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, PersonUpdate>({
    mutationFn: updateOnePerson,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['people'])
      queryClient.invalidateQueries(['person', { id }])
    },
  })
}
