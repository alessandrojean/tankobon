import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import { getOneUser } from '@/services/tankobon-users'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { UserEntity } from '@/types/tankobon-user'

interface UseUserQueryOptions<S = UserEntity> extends UseQueryOptions<UserEntity, ErrorResponse, S> {
  userId: MaybeRef<string>
}

type ErrorResponse = TankobonApiError | Error

export default function useUserQuery<S = UserEntity>(
  options: UseUserQueryOptions<S>,
) {
  return useQuery<UserEntity, ErrorResponse, S>({
    queryKey: ['user', { id: options.userId }],
    queryFn: async () => {
      return await getOneUser(unref(options.userId))
    },
    ...options,
  })
}
