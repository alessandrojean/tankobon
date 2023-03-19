import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { 
  getAuthenticationActivityFromUser,
  GetAuthenticationActivityFromUserOptions
} from '@/services/tankobon-users'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRefDeep } from '@/types/reactivity'
import { AuthenticationActivityEntity } from '@/types/tankobon-authentication-activity'

type UseUserAuthenticationActivityQueryOptions<S = PaginatedResponse<AuthenticationActivityEntity>> =
  UseQueryOptions<PaginatedResponse<AuthenticationActivityEntity>, ErrorResponse, S> 
  & MaybeRefDeep<GetAuthenticationActivityFromUserOptions>

type ErrorResponse = TankobonApiError | Error

export default function useUserAuthenticationActivityQuery<S = PaginatedResponse<AuthenticationActivityEntity>>(
  options: UseUserAuthenticationActivityQueryOptions<S>
) {
  return useQuery<PaginatedResponse<AuthenticationActivityEntity>, ErrorResponse, S>({
    queryKey: [
      'authentication-activity',
      {
        userId: options.userId,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      }
    ],
    queryFn: async () => {
      return await getAuthenticationActivityFromUser({ 
        userId: unref(options.userId),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    keepPreviousData: true,
    ...options,
  })
}
