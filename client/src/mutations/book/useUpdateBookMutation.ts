import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { BookUpdate } from '@/types/tankobon-book'
import { updateOneBook } from '@/services/tankobon-books'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateBookMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, BookUpdate>({
    mutationFn: updateOneBook,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['books'])
      queryClient.invalidateQueries(['book', { id }])
    },
  })
}
