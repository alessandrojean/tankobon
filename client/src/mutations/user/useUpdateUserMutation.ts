import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { updateUser } from '@/services/tankobon-users'
import type { UserUpdate } from '@/types/tankobon-user'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateUserMutation() {
  const queryClient = useQueryClient()
  const userStore = useUserStore()

  return useMutation<void, ErrorResponse, UserUpdate>({
    mutationFn: updateUser,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['users'])
      queryClient.invalidateQueries(['user', { id }])

      if (id === userStore.me?.id) {
        await userStore.checkSession()
      }
    },
  })
}
