import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteStorePicture } from '@/services/tankobon-stores'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteStorePictureMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteStorePicture,
    async onSuccess(_, storeId) {
      queryClient.invalidateQueries(['stores'])
      queryClient.invalidateQueries(['store', { id: storeId }])
    },
  })
}
