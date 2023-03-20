import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneStore } from '@/services/tankobon-stores'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteStoreMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneStore,
    onSuccess(_, storeId) {
      queryClient.invalidateQueries(['stores'])
      queryClient.invalidateQueries(['store', { id: storeId }])
    }
  })
}
