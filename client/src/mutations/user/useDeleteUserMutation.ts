import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneUser } from '@/services/tankobon-users'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteUserMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneUser,
    onSuccess(_, userId) {
      queryClient.invalidateQueries(['users'])
      queryClient.invalidateQueries(['user', { id: userId }])
      queryClient.invalidateQueries(['authentication-activity', { userId }])
    },
  })
}
