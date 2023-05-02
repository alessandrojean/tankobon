import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { CollectionUpdate } from '@/types/tankobon-collection'
import { updateOneCollection } from '@/services/tankobon-collections'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateCollectionMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, CollectionUpdate>({
    mutationFn: updateOneCollection,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['collections'])
      queryClient.invalidateQueries(['collection', { id }])
    },
  })
}
