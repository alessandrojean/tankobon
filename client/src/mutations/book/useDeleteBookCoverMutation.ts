import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteBookCover } from '@/services/tankobon-books'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteBookCoverMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteBookCover,
    async onSuccess(_, bookId) {
      queryClient.invalidateQueries(['books'])
      queryClient.invalidateQueries(['book', { id: bookId }])
    },
  })
}
