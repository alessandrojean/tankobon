import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ReadProgressCreation, ReadProgressEntity } from '@/types/tankobon-read-progress'
import { addOneReadProgress } from '@/services/tankobon-read-progresses'

type ErrorResponse = TankobonApiError | Error

export default function useCreateReadProgressMutation() {
  const queryClient = useQueryClient()

  return useMutation<ReadProgressEntity, ErrorResponse, ReadProgressCreation>({
    mutationFn: addOneReadProgress,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['read-progresses', { bookId: creation.book }])
    },
  })
}
