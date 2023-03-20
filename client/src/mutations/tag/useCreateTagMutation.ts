import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneTag } from '@/services/tankobon-tags'
import type { TagCreation, TagEntity } from '@/types/tankobon-tag'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateTagMutation() {
  const queryClient = useQueryClient()

  return useMutation<TagEntity, ErrorResponse, TagCreation>({
    mutationFn: addOneTag,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['tags', { libraryId: creation.library }])
    }
  })
}
