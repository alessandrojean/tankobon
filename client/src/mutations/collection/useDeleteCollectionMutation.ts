import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneCollection } from '@/services/tankobon-collections'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteCollectionMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneCollection,
    onSuccess(_, collectionId) {
      queryClient.invalidateQueries(['collections'])
      queryClient.invalidateQueries(['collection', { id: collectionId }])
    },
  })
}
