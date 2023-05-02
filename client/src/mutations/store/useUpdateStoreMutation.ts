import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { StoreUpdate } from '@/types/tankobon-store'
import { updateOneStore } from '@/services/tankobon-stores'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateStoreMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, StoreUpdate>({
    mutationFn: updateOneStore,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['stores'])
      queryClient.invalidateQueries(['store', { id }])
    },
  })
}
