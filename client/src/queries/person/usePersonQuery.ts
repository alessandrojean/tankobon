import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { Includes } from '@/types/tankobon-entity'
import { getOnePerson } from '@/services/tankobon-people'
import { PersonEntity } from '@/types/tankobon-person'

interface UsePersonQueryOptions<S = PersonEntity> extends UseQueryOptions<PersonEntity, ErrorResponse, S> {
  personId: MaybeRef<string>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function usePersonQuery<S = PersonEntity>(
  options: UsePersonQueryOptions<S>
) {
  return useQuery<PersonEntity, ErrorResponse, S>({
    queryKey: ['person', { id: options.personId }],
    queryFn: async () => {
      return await getOnePerson({
        personId: unref(options.personId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
