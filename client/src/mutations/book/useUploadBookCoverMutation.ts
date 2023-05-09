import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { UploadBookCoverOptions } from '@/services/tankobon-books'
import { uploadBookCover } from '@/services/tankobon-books'

type ErrorResponse = TankobonApiError | Error

export default function useUploadBookCoverMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, UploadBookCoverOptions>({
    mutationFn: uploadBookCover,
    async onSuccess(_, { bookId }) {
      queryClient.invalidateQueries(['books'])
      queryClient.invalidateQueries(['book', { id: bookId }])
    },
  })
}
