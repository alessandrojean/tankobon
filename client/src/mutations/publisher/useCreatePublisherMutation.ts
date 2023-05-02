import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOnePublisher } from '@/services/tankobon-publishers'
import type { PublisherCreation, PublisherEntity } from '@/types/tankobon-publisher'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreatePublisherMutation() {
  const queryClient = useQueryClient()

  return useMutation<PublisherEntity, ErrorResponse, PublisherCreation>({
    mutationFn: addOnePublisher,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['publishers', { libraryId: creation.library }])
    },
  })
}
