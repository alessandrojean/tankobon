import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneLibrary } from '@/services/tankobon-libraries'
import type { LibraryCreation, LibraryEntity } from '@/types/tankobon-library'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateLibraryMutation() {
  const queryClient = useQueryClient()

  return useMutation<LibraryEntity, ErrorResponse, LibraryCreation>({
    mutationFn: addOneLibrary,
    onSuccess() {
      queryClient.invalidateQueries(['libraries'])
      queryClient.invalidateQueries(['libraries-owned'])
    },
  })
}
