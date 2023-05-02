import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import { getOnePerson } from '@/services/tankobon-people'
import type { PersonEntity, PersonIncludes } from '@/types/tankobon-person'

interface UsePersonQueryOptions<S = PersonEntity> extends UseQueryOptions<PersonEntity, ErrorResponse, S> {
  personId: MaybeRef<string>
  includes?: MaybeRef<PersonIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function usePersonQuery<S = PersonEntity>(
  options: UsePersonQueryOptions<S>,
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
