import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneLibrary } from '@/services/tankobon-libraries'

export default function useCreateLibraryMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: addOneLibrary,
    onSuccess() {
      queryClient.invalidateQueries(['libraries'])
    }
  })
}
