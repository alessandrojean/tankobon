import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { getAllLibraries } from '@/services/tankobon-libraries'
import type { LibraryEntity } from '@/types/tankobon-library'
import type { Includes } from '@/types/tankobon-entity'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { MaybeRef } from '@vueuse/core'

interface UseUserLibrariesQueryOptions<S = LibraryEntity[]> 
  extends UseQueryOptions<LibraryEntity[], ErrorResponse, S> {
  ownerId?: MaybeRef<string | undefined>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useUserLibrariesQuery<S = LibraryEntity[]>(
  options?: UseUserLibrariesQueryOptions<S>
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
