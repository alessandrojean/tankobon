import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneBook } from '@/services/tankobon-books'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteBookMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneBook,
    onSuccess(_, bookId) {
      queryClient.invalidateQueries(['books'])
      queryClient.invalidateQueries(['book', { id: bookId }])
    },
  })
}
