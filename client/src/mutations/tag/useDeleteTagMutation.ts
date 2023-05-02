import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneTag } from '@/services/tankobon-tags'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteTagMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneTag,
    onSuccess(_, tagId) {
      queryClient.invalidateQueries(['tags'])
      queryClient.invalidateQueries(['tag', { id: tagId }])
    },
  })
}
