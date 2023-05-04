import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { type UploadPublisherPictureOptions, uploadPublisherPicture } from '@/services/tankobon-publishers'

type ErrorResponse = TankobonApiError | Error

export default function useUploadPublisherPictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, UploadPublisherPictureOptions>({
    mutationFn: uploadPublisherPicture,
    async onSuccess(_, { publisherId }) {
      queryClient.invalidateQueries(['publishers'])
      queryClient.invalidateQueries(['publisher', { id: publisherId }])
    },
  })
}
