import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { UploadStorePictureOptions } from '@/services/tankobon-stores'
import { uploadStorePicture } from '@/services/tankobon-stores'

type ErrorResponse = TankobonApiError | Error

export default function useUploadStorePictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, UploadStorePictureOptions>({
    mutationFn: uploadStorePicture,
    async onSuccess(_, { storeId }) {
      queryClient.invalidateQueries(['stores'])
      queryClient.invalidateQueries(['store', { id: storeId }])
    },
  })
}
