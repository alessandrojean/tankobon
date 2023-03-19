import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { LibraryUpdate } from '@/types/tankobon-library'
import { updateOneLibrary } from '@/services/tankobon-libraries'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateLibraryMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, LibraryUpdate>({
    mutationFn: updateOneLibrary,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['libraries'])
      queryClient.invalidateQueries(['libraries-owned'])
      queryClient.invalidateQueries(['library', { id }])
    }
  })
}
