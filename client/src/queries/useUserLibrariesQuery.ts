import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { getAllLibraries } from '@/services/tankobon-libraries'
import type { TankobonLibraryEntity } from '@/types/tankobon-library'
import type { Includes } from '@/types/tankobon-entity'

interface UseUserLibrariesQueryOptions extends UseQueryOptions<TankobonLibraryEntity[], Error> {
  ownerId?: string,
  includes?: Includes
}

export default function useUserLibrariesQuery(options?: UseUserLibrariesQueryOptions) {
  const { ownerId, includes } = (options ?? {})

  return useQuery<TankobonLibraryEntity[], Error>({
    queryKey: ['libraries', { ownerId, includes }],
    queryFn: async () => {
      return await getAllLibraries({ ownerId, includes })
    },
    ...options,
  })
}
