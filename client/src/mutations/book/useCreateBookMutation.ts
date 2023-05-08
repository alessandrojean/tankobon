import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { BookEntity, BookUpdate } from '@/types/tankobon-book'
import { addOneBook } from '@/services/tankobon-books'

type ErrorResponse = TankobonApiError | Error

export default function useCreateBookMutation() {
  const queryClient = useQueryClient()

  return useMutation<BookEntity, ErrorResponse, BookUpdate>({
    mutationFn: addOneBook,
    async onSuccess() {
      queryClient.invalidateQueries(['books'])
    },
  })
}
