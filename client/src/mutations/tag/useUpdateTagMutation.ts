import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { TagUpdate } from '@/types/tankobon-tag'
import { updateOneTag } from '@/services/tankobon-tags'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateTagMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, TagUpdate>({
    mutationFn: updateOneTag,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['tags'])
      queryClient.invalidateQueries(['tag', { id }])
    }
  })
}
