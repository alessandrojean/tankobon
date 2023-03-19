import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { LibraryEntity } from '@/types/tankobon-library'
import { Includes } from '@/types/tankobon-entity'
import { getOneLibrary } from '@/services/tankobon-libraries'

interface UseLibraryQueryOptions<S = LibraryEntity> extends UseQueryOptions<LibraryEntity, ErrorResponse, S> {
  libraryId: MaybeRef<string>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useLibraryQuery<S = LibraryEntity>(
  options: UseLibraryQueryOptions<S>
) {
  return useQuery<LibraryEntity, ErrorResponse, S>({
    queryKey: ['library', { id: options.libraryId }],
    queryFn: async () => {
      return await getOneLibrary({
        libraryId: unref(options.libraryId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
