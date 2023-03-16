import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { getAllUsers, type GetAllUsersOptions } from '@/services/tankobon-users'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { UserEntity } from '@/types/tankobon-user'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseUsersQueryOptions<S = PaginatedResponse<UserEntity>> =
  UseQueryOptions<PaginatedResponse<UserEntity>, ErrorResponse, S> 
  & MaybeRefDeep<GetAllUsersOptions>

type ErrorResponse = TankobonApiError | Error

export default function useUsersQuery<S = PaginatedResponse<UserEntity>>(
  options?: UseUsersQueryOptions<S>
) {
  return useQuery<PaginatedResponse<UserEntity>, ErrorResponse, S>({
    queryKey: [
      'users',
      {
        page: options?.page,
        sort: options?.sort,
        size: options?.size,
        includes: options?.includes,
      }
    ],
    queryFn: async () => {
      return await getAllUsers({ 
        page: unref(options?.page),
        sort: unref(options?.sort),
        size: unref(options?.size),
        includes: unref(options?.includes),
      })
    },
    keepPreviousData: true,
    ...options,
  })
}
