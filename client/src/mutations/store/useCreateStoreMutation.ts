import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneStore } from '@/services/tankobon-stores'
import type { StoreCreation, StoreEntity } from '@/types/tankobon-store'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateStoreMutation() {
  const queryClient = useQueryClient()

  return useMutation<StoreEntity, ErrorResponse, StoreCreation>({
    mutationFn: addOneStore,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['stores', { libraryId: creation.library }])
    }
  })
}
