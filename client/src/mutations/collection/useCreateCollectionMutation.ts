import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneCollection } from '@/services/tankobon-collections'
import type { CollectionCreation, CollectionEntity } from '@/types/tankobon-collection'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateCollectionMutation() {
  const queryClient = useQueryClient()

  return useMutation<CollectionEntity, ErrorResponse, CollectionCreation>({
    mutationFn: addOneCollection,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['collections', { libraryId: creation.library }])
    }
  })
}
