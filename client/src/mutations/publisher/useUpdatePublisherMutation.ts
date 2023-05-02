import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { PublisherUpdate } from '@/types/tankobon-publisher'
import { updateOnePublisher } from '@/services/tankobon-publishers'

type ErrorResponse = TankobonApiError | Error

export default function useUpdatePublisherMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, PublisherUpdate>({
    mutationFn: updateOnePublisher,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['publishers'])
      queryClient.invalidateQueries(['publisher', { id }])
    },
  })
}
