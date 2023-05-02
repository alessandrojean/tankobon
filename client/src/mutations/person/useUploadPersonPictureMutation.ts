import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { type UploadPersonPictureOptions, uploadPersonPicture } from '@/services/tankobon-people'

type ErrorResponse = TankobonApiError | Error

export default function useUploadPersonPictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, UploadPersonPictureOptions>({
    mutationFn: uploadPersonPicture,
    async onSuccess(_, { personId }) {
      queryClient.invalidateQueries(['people'])
      queryClient.invalidateQueries(['person', { id: personId }])
    },
  })
}
