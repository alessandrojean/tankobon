import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneLibrary } from '@/services/tankobon-libraries'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteLibraryMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneLibrary,
    onSuccess(_, libraryId) {
      queryClient.invalidateQueries(['libraries'])
      queryClient.invalidateQueries(['libraries-owned'])
      queryClient.invalidateQueries(['library', { id: libraryId }])
    },
  })
}
