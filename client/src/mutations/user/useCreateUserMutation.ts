import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { UserCreation, UserEntity } from '@/types/tankobon-user'
import { addOneUser } from '@/services/tankobon-users'

type ErrorResponse = TankobonApiError | Error

export default function useCreateUserMutation() {
  const queryClient = useQueryClient()

  return useMutation<UserEntity, ErrorResponse, UserCreation>({
    mutationFn: addOneUser,
    onSuccess() {
      queryClient.invalidateQueries(['users'])
    }
  })
}
