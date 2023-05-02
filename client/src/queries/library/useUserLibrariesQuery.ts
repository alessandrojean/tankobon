import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import { getAllLibraries } from '@/services/tankobon-libraries'
import type { LibraryEntity, LibraryIncludes } from '@/types/tankobon-library'
import type { TankobonApiError } from '@/types/tankobon-response'

interface UseUserLibrariesQueryOptions<S = LibraryEntity[]>
  extends UseQueryOptions<LibraryEntity[], ErrorResponse, S> {
  ownerId?: MaybeRef<string | undefined>
  includes?: MaybeRef<LibraryIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function useUserLibrariesQuery<S = LibraryEntity[]>(
  options?: UseUserLibrariesQueryOptions<S>,
) {
  const { ownerId, includes } = (options ?? {})

  return useQuery<LibraryEntity[], ErrorResponse, S>({
    queryKey: ['libraries', { ownerId, includes }],
    queryFn: async () => {
      return await getAllLibraries({
        ownerId: unref(ownerId),
        includes: unref(includes),
      })
    },
    ...options,
  })
}
