import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneLibrary } from '@/services/tankobon-libraries'
import type { AddOneLibrary, TankobonLibraryEntity } from '@/types/tankobon-library'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateLibraryMutation() {
  const queryClient = useQueryClient()

  return useMutation<TankobonLibraryEntity, ErrorResponse, AddOneLibrary>({
    mutationFn: addOneLibrary,
    onSuccess() {
      queryClient.invalidateQueries(['libraries'])
    }
  })
}
