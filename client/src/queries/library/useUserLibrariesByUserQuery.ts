import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { GetAllLibrariesByUserParameters } from '@/services/tankobon-libraries'
import { getAllLibrariesByUser } from '@/services/tankobon-libraries'
import type { LibraryEntity } from '@/types/tankobon-library'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseUserLibrariesByUserQueryOptions<S = PaginatedResponse<LibraryEntity>> =
  UseQueryOptions<PaginatedResponse<LibraryEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllLibrariesByUserParameters>

type ErrorResponse = TankobonApiError | Error

export default function useUserLibrariesByUserQuery<S = PaginatedResponse<LibraryEntity>>(
  options: UseUserLibrariesByUserQueryOptions<S> = {},
) {
  return useQuery<PaginatedResponse<LibraryEntity>, ErrorResponse, S>({
    queryKey: [
      'libraries',
      {
        userId: options.userId,
        includeShared: options.includeShared,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
        unpaged: options.unpaged,
      },
    ],
    queryFn: async () => {
      return await getAllLibrariesByUser({
        userId: unref(options.userId),
        includeShared: unref(options.includeShared),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
        unpaged: unref(options.unpaged),
      })
    },
    ...options,
  })
}
