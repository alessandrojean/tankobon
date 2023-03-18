import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { getAllLibrariesByUser } from '@/services/tankobon-libraries'
import type { LibraryEntity } from '@/types/tankobon-library'
import type { Includes } from '@/types/tankobon-entity'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRef } from '@vueuse/core'

interface UseUserLibrariesQueryByUserOptions<S = LibraryEntity[]> 
  extends UseQueryOptions<LibraryEntity[], ErrorResponse, S> {
  userId: MaybeRef<string>,
  includeShared?: MaybeRef<boolean>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useUserLibrariesByUserQuery<S = LibraryEntity[]>(
  options: UseUserLibrariesQueryByUserOptions<S>
) {
  const { userId, includes, includeShared } = (options ?? {})

  return useQuery<LibraryEntity[], ErrorResponse, S>({
    queryKey: ['libraries-owned', { userId, includes, includeShared }],
    queryFn: async () => {
      return await getAllLibrariesByUser({ 
        userId: unref(userId),
        includeShared: unref(includeShared),
        includes: unref(includes),
      })
    },
    ...options,
  })
}
